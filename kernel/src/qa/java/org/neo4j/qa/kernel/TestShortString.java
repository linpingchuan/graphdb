package org.neo4j.qa.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.helpers.collection.MapUtil.map;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.AbstractNeo4jTestCase;

public class TestShortString
{
    private static final String DEFAULT_SHORT_STRING = "test";
    
    private static final String PATH = "target/var/qa-shortstring";
    private GraphDatabaseService db;
    
    @Before
    public void doBefore()
    {
        AbstractNeo4jTestCase.deleteFileOrDirectory( new File( PATH ) );
        db = new EmbeddedGraphDatabase( PATH );
    }
    
    @After
    public void doAfter()
    {
        db.shutdown();
    }
    
    @Test
    public void justToMakeSureThisQaTestSuiteHoldsWater() throws Exception
    {
        String key = "type";
        String aLongString = "This string should definately not fit in a shortstring";
        long sizeBefore = getSizeOfStringStore();
        createNode( map( key, aLongString ) );
        long sizeAfter = getSizeOfStringStore();
        assertTrue( sizeAfter > sizeBefore );
    }
    
    @Test
    public void makeSureNoDynamicRecordIsCreatedForShortStringOnEmptyStore() throws Exception
    {
        makeSureAShortStringWontGrowStringStore( DEFAULT_SHORT_STRING );
    }

    @Test
    public void makeSureNoDynamicRecordIsCreatedForShortStringOnExistingData() throws Exception
    {
        createNode( map( "name", "Neo" ) );
        createNode( map( "name", "A longer name, not fit for shortstring" ) );
        
        makeSureAShortStringWontGrowStringStore( DEFAULT_SHORT_STRING );
    }
    
    @Test
    public void makeSureSomeDynamicRecordIsCreatedForStringOnExistingData() throws Exception
    {
        createNode( map( "name", "Neo" ) );
        createNode( map( "name", "A longer name, not fit for shortstring" ) );
        
        makeSureAShortStringWillGrowStringStore( "$1\"üedsa" );
    }
    
    @Test
    public void makeSureNumericalShortStringJustBelowLimitWontGrowStringStore() throws Exception
    {
        makeSureAShortStringWontGrowStringStore( "+123456789 1234" );
    }
    
    @Test
    public void makeSureNumericalShortStringJustAboveLimitGrowStringStore() throws Exception
    {
        makeSureAShortStringWillGrowStringStore( "+123456789 01234" );
    }
    
    @Test
    public void makeSureCaseShortStringJustBelowLimitWontGrowStringStore() throws Exception
    {
        makeSureAShortStringWontGrowStringStore( "SOME_VALUE:T" );
    }
    
    @Test
    public void makeSureCaseShortStringJustAboveLimitGrowStringStore() throws Exception
    {
        makeSureAShortStringWillGrowStringStore( "SOMETHING_LON" );
    }
    
    @Test
    public void makeSureAlphaNumericalShortStringJustBelowLimitWontGrowStringStore() throws Exception
    {
        makeSureAShortStringWontGrowStringStore( "Mattias Yu" );
    }
    
    @Test
    public void makeSureAlphaNumericalShortStringJustAboveLimitGrowStringStore() throws Exception
    {
        makeSureAShortStringWillGrowStringStore( "Mattias Yup" );
    }
    
    @Test
    public void makeSureUtf8ShortStringJustBelowLimitWontGrowStringStore() throws Exception
    {
        makeSureAShortStringWontGrowStringStore( "¡@$#abc" );
    }
    
    @Test
    public void makeSureUtf8ShortStringJustAboveLimitGrowStringStore() throws Exception
    {
        makeSureAShortStringWillGrowStringStore( "¡@$#abcd" );
    }
    
    private void makeSureAShortStringWontGrowStringStore( String shortString )
    {
        assertEquals( 0, stringStoreDiff( shortString ) );
    }
    
    private void makeSureAShortStringWillGrowStringStore( String shortString )
    {
        assertTrue( stringStoreDiff( shortString ) > 0 );
    }
    
    private long stringStoreDiff( String propertyValue )
    {
        String key = "type";
        long sizeBefore = getSizeOfStringStore();
        long node = createNode( map( key, propertyValue ) );
        long sizeAfter = getSizeOfStringStore();
        assertEquals( propertyValue, db.getNodeById( node ).getProperty( key ) );
        return sizeAfter - sizeBefore;
    }
    
    private long getSizeOfStringStore()
    {
        db.shutdown();
        long size = new File( PATH, "neostore.propertystore.db.strings" ).length();
        db = new EmbeddedGraphDatabase( PATH );
        return size;
    }

    private long createNode( Map<String, Object> properties )
    {
        Transaction tx = db.beginTx();
        Node node = db.createNode();
        for ( Map.Entry<String, Object> property : properties.entrySet() )
        {
            node.setProperty( property.getKey(), property.getValue() );
        }
        tx.success();
        tx.finish();
        return node.getId();
    }
}