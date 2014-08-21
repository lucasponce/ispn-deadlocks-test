import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Copyright (C) 2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestConcurrentPut
{
    // The default total amount of threads knowing that in udp-mux.xml,
    // thread_pool.max_threads and oob_thread_pool.max_threads have been set to 20
    private static final int DEFAULT_TOTAL_THREADS = 20;

    public static void main(final String... args) throws Exception
    {
        System.setProperty("jgroups.bind_addr", "127.0.0.1");
        EmbeddedCacheManager manager = new DefaultCacheManager("test-infinispan-config.xml");
        final Cache<String, String> cache = manager.getCache();
        System.out.println("Wait until the cluster is ready");
        while (manager.getMembers().size() <= 1);
        System.out.println("The cluster is ready");
        final CountDownLatch startSignal = new CountDownLatch(1);
        final AtomicLong counter = new AtomicLong();
        final String nodeId;
        int totalThreads = DEFAULT_TOTAL_THREADS;
        if (args.length > 0)
        {
            nodeId = args[0];
            if (args.length > 1)
            {
                totalThreads = Integer.parseInt(args[1]);
            }
        }
        else
        {
            nodeId = Integer.toString(System.identityHashCode(cache));
        }
        System.out.println("The id of this node is " + nodeId);
        System.out.println("The total amount of threads for this node is " + totalThreads);
        Runnable task = new Runnable()
        {
            public void run()
            {
                try
                {
                    startSignal.await();
                }
                catch (InterruptedException e1)
                {
                    e1.printStackTrace();
                    return;
                }
                while (true)
                {
                    try
                    {
                        String value = cache.get("a");
                        long call = counter.incrementAndGet();
                        if ((call % 1000) == 0)
                            System.out.println(call + ". " + Thread.currentThread() + ": a=" + value + " from " + nodeId);
                        cache.put("a", nodeId);
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        for (int i = 0; i < totalThreads; i++)
            new Thread(task).start();
        startSignal.countDown();
    }

}
