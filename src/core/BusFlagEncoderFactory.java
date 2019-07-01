/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package core;

import com.graphhopper.routing.util.Bike2WeightFlagEncoder;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.Car4WDFlagEncoder;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.DataFlagEncoder;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FlagEncoderFactory;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.util.HikeFlagEncoder;
import com.graphhopper.routing.util.MotorcycleFlagEncoder;
import com.graphhopper.routing.util.MountainBikeFlagEncoder;
import com.graphhopper.routing.util.RacingBikeFlagEncoder;
import com.graphhopper.util.PMap;

/**
 * This class creates FlagEncoders that are already included in the GraphHopper distribution.
 * I am not the author,
 * but I have modified it in order to add my own Bus Flag Encoder to GraphHopper Encoding Manager
 * 
 * (BUS edit by Francesco Raco)
 *
 * @author Peter Karich
 */
public class BusFlagEncoderFactory implements FlagEncoderFactory
{
	/**
	 * BUS string representing the Bus Flag Encoder
	 */
	public static final String BUS = "bus";
	
    /**
     * Create flag encoder by String name and  Pmap configuration;
     * Edit allowing Bus Flag Encoder Creation
     * @param name String name
     * @param configuration PMap configuration
     * @return FlagEncoder Specific Flag Encoder created
     */
	@Override
    public FlagEncoder createFlagEncoder(String name, PMap configuration)
    {
        if (name.equals(GENERIC))
            return new DataFlagEncoder(configuration);

        else if (name.equals(CAR))
            return new CarFlagEncoder(configuration);

        else if (name.equals(CAR4WD))
            return new Car4WDFlagEncoder(configuration);

        if (name.equals(BIKE))
            return new BikeFlagEncoder(configuration);

        if (name.equals(BIKE2))
            return new Bike2WeightFlagEncoder(configuration);

        if (name.equals(RACINGBIKE))
            return new RacingBikeFlagEncoder(configuration);

        if (name.equals(MOUNTAINBIKE))
            return new MountainBikeFlagEncoder(configuration);

        if (name.equals(FOOT))
            return new FootFlagEncoder(configuration);

        if (name.equals(HIKE))
            return new HikeFlagEncoder(configuration);

        if (name.equals(MOTORCYCLE))
            return new MotorcycleFlagEncoder(configuration);
        
        //This is my edit
        if (name.equals(BUS))
            return new BusFlagEncoder(configuration);

        throw new IllegalArgumentException("entry in encoder list not supported " + name);
    }
}