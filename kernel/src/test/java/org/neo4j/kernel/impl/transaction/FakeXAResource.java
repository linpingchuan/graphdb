/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.transaction;

import java.util.ArrayList;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

class FakeXAResource implements XAResource
{
    private String name = null;
    private int transactionTimeout = 0;
    private ArrayList<MethodCall> methodCalls = new ArrayList<MethodCall>();

    FakeXAResource( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return name;
    }

    synchronized MethodCall[] getAndRemoveMethodCalls()
    {
        if ( methodCalls.size() > 0 )
        {
            MethodCall methodCallArray[] = new MethodCall[methodCalls.size()];
            methodCallArray = methodCalls.toArray( methodCallArray );
            methodCalls = new ArrayList<MethodCall>();
            return methodCallArray;
        }
        return new MethodCall[0];
    }

    private synchronized void addMethodCall( MethodCall methodCall )
    {
        methodCalls.add( methodCall );
    }

    public void commit( Xid xid, boolean onePhase )
    {
        addMethodCall( new MethodCall( "commit", new Object[] { xid,
            new Boolean( onePhase ) }, new String[] {
            "javax.transaction.xa.Xid", "java.lang.Boolean" } ) );
    }

    public void end( Xid xid, int flags )
    {
        addMethodCall( new MethodCall( "end", new Object[] { xid,
            new Integer( flags ) }, new String[] { "javax.transaction.xa.Xid",
            "java.lang.Integer" } ) );
    }

    public void forget( Xid xid )
    {
        addMethodCall( new MethodCall( "forget", new Object[] { xid },
            new String[] { "javax.transaction.xa.Xid" } ) );
    }

    public int getTransactionTimeout()
    {
        return transactionTimeout;
    }

    public boolean setTransactionTimeout( int timeout )
    {
        transactionTimeout = timeout;
        return true;
    }

    public boolean isSameRM( XAResource xares )
    {
        if ( xares instanceof FakeXAResource )
        {
            if ( this.name.equals( ((FakeXAResource) xares).getName() ) )
            {
                return true;
            }
        }
        return false;
    }

    public int prepare( Xid xid )
    {
        addMethodCall( new MethodCall( "prepare", new Object[] { xid },
            new String[] { "javax.transaction.xa.Xid" } ) );
        return XAResource.XA_OK;
    }

    public Xid[] recover( int flag )
    {
        addMethodCall( new MethodCall( "recover", new Object[] { new Integer(
            flag ) }, new String[] { "java.lang.Integer" } ) );
        return new Xid[0];
    }

    public void rollback( Xid xid )
    {
        addMethodCall( new MethodCall( "rollback", new Object[] { xid },
            new String[] { "javax.transaction.xa.Xid" } ) );
    }

    public void start( Xid xid, int flags )
    {
        addMethodCall( new MethodCall( "start", new Object[] { xid,
            new Integer( flags ) }, new String[] { "javax.transaction.xa.Xid",
            "java.lang.Integer" } ) );
    }
}