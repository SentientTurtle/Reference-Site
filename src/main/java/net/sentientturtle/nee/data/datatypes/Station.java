package net.sentientturtle.nee.data.datatypes;

import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public class Station {
    public final int stationID;
    public final int stationTypeID;
    public final String stationName;
    public final EnumSet<Service> services;

    public Station(int stationID, int stationTypeID, String stationName, EnumSet<Service> services) {
        this.stationID = stationID;
        this.stationTypeID = stationTypeID;
        this.stationName = stationName;
        this.services = services;
    }

    public enum Service {
        REPROCESSING(16, "Reprocessing Plant", "res:/ui/texture/windowicons/reprocess.png"),
        MARKET(64, "Market", "res:/ui/texture/windowicons/market.png"),
        CLONEBAY(512, "Clone Bay", "res:/ui/texture/windowicons/clonebay.png"),
        REPAIRSHOP(4096, "Repairshop", "res:/ui/texture/windowicons/repairshop.png"),
        INDUSTRY(8192, "Industry", "res:/ui/texture/windowicons/industry.png"),
        FITTING(65536, "Fitting", "res:/ui/texture/windowicons/fitting.png"),
        INSURANCE(1048576, "Insurance", "res:/ui/texture/windowicons/insurance.png"),
        LPSTORE(16777216, "LP Store", "res:/ui/texture/windowicons/lpstore.png"),
        MILITIAOFFICE(33554432, "Militia Office", "res:/ui/texture/windowicons/factionalwarfare.png");

        public final int seviceID;
        public final String displayName;
        public final String iconResource;

        Service(int serviceID, String displayName, String iconResource) {
            this.seviceID = serviceID;
            this.displayName = displayName;
            this.iconResource = iconResource;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Station station)) return false;
        return stationID == station.stationID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stationID);
    }
}
