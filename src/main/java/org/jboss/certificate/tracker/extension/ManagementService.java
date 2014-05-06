/** Copyright 2014 Filip Bogyai
 *
 * This file is part of certificate-tracker.
 *
 * Certificate-tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.jboss.certificate.tracker.extension;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.certificate.tracker.core.ServerKeystoreReload;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class ManagementService implements Service<ManagementService> {

    public InjectedValue<ModelController> modelController = new InjectedValue<ModelController>();
    private ModelControllerClient controllerClient;
    private ExecutorService executor;

    @Override
    public ManagementService getValue() throws IllegalStateException, IllegalArgumentException {

        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        executor = Executors.newFixedThreadPool(5, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName(String.valueOf(System.currentTimeMillis()));
                return t;
            }
        });
        controllerClient = modelController.getValue().createClient(executor);
        ServerKeystoreReload.INSTANCE.setManagementClient(controllerClient);
    }

    @Override
    public void stop(StopContext context) {
        try {
            executor.shutdownNow();
        } finally {
            executor = null;
            controllerClient = null;
        }

    }

    public static ServiceName getServiceName() {
        return ServiceName.JBOSS.append("management-client-getter");
    }

}
