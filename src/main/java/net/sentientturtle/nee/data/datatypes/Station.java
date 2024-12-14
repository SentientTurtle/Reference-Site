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

        public static Station.@Nullable Service fromServiceID(int serviceID) {
            if (Integer.bitCount(serviceID) != 1) throw new IllegalArgumentException("ServiceID must be a single set bit flag!");

            return switch (serviceID) {
                case 16 -> Service.REPROCESSING;
                case 64 -> Service.MARKET;
                case 512 -> Service.CLONEBAY;
                case 4096 -> Service.REPAIRSHOP;
                case 8192 -> Service.INDUSTRY;
                case 1048576 -> Service.INSURANCE;
                default -> null;

//                case 1 -> throw new IllegalArgumentException("Unsupported service: Bounty Missions");
//                case 2 -> throw new IllegalArgumentException("Unsupported service: Assassination Missions");
//                case 4 -> throw new IllegalArgumentException("Unsupported service: Courier Missions");
//                case 8 -> throw new IllegalArgumentException("Unsupported service: Interbus");
//                case 32 -> throw new IllegalArgumentException("Unsupported service: Refinery");
//                case 128 -> throw new IllegalArgumentException("Unsupported service: Black Market");
//                case 256 -> throw new IllegalArgumentException("Unsupported service: Stock Exchange");
//                case 1024 -> throw new IllegalArgumentException("Unsupported service: Surgery");
//                case 2048 -> throw new IllegalArgumentException("Unsupported service: DNA Therapy");
//                case 16384 -> throw new IllegalArgumentException("Unsupported service: Laboratory");
//                case 32768 -> throw new IllegalArgumentException("Unsupported service: Gambling");
//                case 131072 -> throw new IllegalArgumentException("Unsupported service: Paintshop");
//                case 262144 -> throw new IllegalArgumentException("Unsupported service: News");
//                case 524288 -> throw new IllegalArgumentException("Unsupported service: Storage");
//                case 2097152 -> throw new IllegalArgumentException("Unsupported service: Docking"); // For use in citadels?
//                case 4194304 -> throw new IllegalArgumentException("Unsupported service: Office Rental"); // For use in citadels?
//                default -> throw new IllegalArgumentException("Unknown service ID: " + serviceID);
            };
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
