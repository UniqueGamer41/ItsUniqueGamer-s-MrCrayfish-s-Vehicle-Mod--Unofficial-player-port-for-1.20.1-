package com.mrcrayfish.vehicle.client.model;

import com.mrcrayfish.vehicle.Reference;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.units.qual.C;

public class VehicleModels
{
    public static final ComponentLoader LOADER = new ComponentLoader(Reference.MOD_ID);

    /* Sports Car */
    public static final ComponentModel SPORTS_CAR_BODY = LOADER.create("item/sports_car_base");
    public static final ComponentModel MINI_BUS_KEYHOLE = LOADER.create("item/key_hole_mbu");
    public static final ComponentModel SPORTS_CAR_STEERING_WHEEL = LOADER.create("item/sports_car_steering_wheel");
    public static final ComponentModel SPORTS_CAR_COSMETIC_DASHBOARD = LOADER.create("item/sports_car_cosmetic_dashboard");
    public static final ComponentModel SPORTS_CAR_COSMETIC_HOOD = LOADER.create("item/sports_car_cosmetic_hood");
    public static final ComponentModel SPORTS_CAR_COSMETIC_LEFT_DOOR = LOADER.create("item/sports_car_cosmetic_left_door");
    public static final ComponentModel SPORTS_CAR_COSMETIC_RIGHT_DOOR = LOADER.create("item/sports_car_cosmetic_right_door");
    public static final ComponentModel SPORTS_CAR_COSMETIC_SEAT = LOADER.create("item/sports_car_cosmetic_seat");
    public static final ComponentModel SPORTS_CAR_COSMETIC_BOOT = LOADER.create("item/sports_car_cosmetic_boot");
    public static final ComponentModel SPORTS_CAR_COSMETIC_ROOF = LOADER.create("item/sports_car_cosmetic_roof");
    public static final ComponentModel KEYHOLE_SPC =  LOADER.create("item/key_hole_spc");
    public static final ComponentModel KEYHOLE_MOPED = LOADER.create("item/key_hole_moped");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_DASHBOARD = LOADER.create("item/sports_car/cosmetics/stock_dashboard");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_FRONT_BUMPER = LOADER.create("item/sports_car/cosmetics/stock_front_bumper");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_REAR_BUMPER = LOADER.create("item/sports_car_stock_rear_bumper");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_HOOD = LOADER.create("item/sports_car/cosmetics/stock_hood");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_LEFT_DOOR = LOADER.create("item/sports_car/cosmetics/stock_left_door");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_RIGHT_DOOR = LOADER.create("item/sports_car/cosmetics/stock_right_door");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_FRONT_LIGHTS = LOADER.create("item/sports_car/cosmetics/stock_front_lights");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_REAR_LIGHTS = LOADER.create("item/sports_car_stock_rear_lights");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_SEAT = LOADER.create("item/sports_car/cosmetics/stock_seat");
    public static final ComponentModel SPORTS_CAR_COSMETIC_STOCK_SPOILER = LOADER.create("item/sports_car/cosmetics/stock_spoiler");
    public static final ComponentModel KEYHOLE_ATV = LOADER.create("item/key_hole_atv");
    public static final ComponentModel KEYHOLE_CH = LOADER.create("item/key_hole_ch");
    public static final ComponentModel KEYHOLE_DBI = LOADER.create("item/key_hole_dbi");
    public static final ComponentModel KEYHOLE_DBU = LOADER.create("item/key_hole_dbu");
    public static final ComponentModel KEYHOLE_JK = LOADER.create("item/key_hole_jk");
    public static final ComponentModel KEYHOLE_LM = LOADER.create("item/key_hole_lm");
    public static final ComponentModel KEYHOLE_MBI = LOADER.create("item/key_hole_mbi");
    public static final ComponentModel KEYHOLE_QB =  LOADER.create("item/key_hole_qb");
    public static final ComponentModel KEYHOLE_SC = LOADER.create("item/key_hole_sc");
    public static final ComponentModel KEYHOLE_TRACTOR = LOADER.create("item/key_hole_tractor");
    /* Shopping Cart */
    public static final ComponentModel SHOPPING_CART_BODY = LOADER.create("item/shopping_cart_body");
    public static final ComponentModel ATV_BODY = LOADER.create("item/atv_body");
    public static final ComponentModel ATV_HANDLES = LOADER.create("item/atv_handles");

    public static final ComponentModel ALLUMINUM_BOAT_BODY = LOADER.create("item/aluminum_boat_body");

    public static final ComponentModel DUNE_BUGGY_BODY = LOADER.create("item/dune_buggy_body");
    public static final ComponentModel DUNE_BUGGY_HANDLES = LOADER.create("item/dune_buggy_handles");

    public static final ComponentModel GOLF_KH = LOADER.create("item/key_hole_golf");

    public static final ComponentModel KEYHOLE_OFFROADER = LOADER.create("item/key_hole_or");
    /* Bumper car */
    public static final ComponentModel BUMPER_CAR_BODY = LOADER.create("item/bumper_car_body");
    public static final ComponentModel SPORTS_PLANE_KEYHOLE = LOADER.create("item/key_hole_spp");

    /* Mini Bus */
    public static final ComponentModel MINI_BUS_BODY = LOADER.create("item/minibus_body");
    public static final ComponentModel MINI_BUS_STEERING_WHEEL = LOADER.create("item/steering_wheel");
    public static final ComponentModel MINI_BUS_COSMETIC_STOCK_DASHBOARD = LOADER.create("item/minibus_stock_dashboard");
    public static final ComponentModel MINI_BUS_COSMETIC_STOCK_LEFT_DOOR = LOADER.create("item/minibus_stock_left_door");
    public static final ComponentModel MINI_BUS_COSMETIC_STOCK_RIGHT_DOOR = LOADER.create("item/minibus_stock_right_door");
    public static final ComponentModel MINI_BUS_COSMETIC_STOCK_SLIDING_DOOR = LOADER.create("item/stock_sliding_door");
    public static final ComponentModel MINI_BUS_COSMETIC_STOCK_SEAT = LOADER.create("item/minibus_stock_seat");
    public static final ComponentModel MINI_BUS_COSMETIC_STOCK_ROOF = LOADER.create("item/minibus_stock_roof");
    public static final ComponentModel MINI_BUS_COSMETIC_ROOF_RACKS = LOADER.create("item/minibus_roof_racks");
    public static final ComponentModel MINI_BUS_COSMETIC_AIRCON_LADDER_REAR_DECOR = LOADER.create("item/minibus_aircon_ladder");
    public static final ComponentModel MINI_BUS_COSMETIC_FRONT_ROOF = LOADER.create("item/minibus_front_roof");
    public static final ComponentModel KEYHOLE_SB = LOADER.create("item/key_hole_sb");
    /* Moped */
    public static final ComponentModel MOPED_BODY = LOADER.create("item/moped_body");
    public static final ComponentModel MOPED_MUD_GUARD = LOADER.create("item/moped_mud_guard");
    public static final ComponentModel MOPED_HANDLES = LOADER.create("item/moped_handles");
    public static final ComponentModel MOPED_COSMETIC_STOCK_SEAT = LOADER.create("item/moped_stock_seat");
    public static final ComponentModel MOPED_COSMETIC_STOCK_TRAY = LOADER.create("item/moped_stock_tray");
    public static final ComponentModel MOPED_COSMETIC_STOCK_FRONT_LIGHT = LOADER.create("item/moped_stock_front_light");

    /* Dirt Bike */
    public static final ComponentModel DIRT_BIKE_BODY = LOADER.create("item/dirt_bike_body");
    public static final ComponentModel DIRT_BIKE_HANDLES = LOADER.create("item/dirt_bike_handles");

    /* Quad Bike */
    public static final ComponentModel QUAD_BIKE_BODY = LOADER.create("item/quad_bike_base");
    public static final ComponentModel QUAD_BIKE_HANDLES = LOADER.create("item/quad_bike_handles");

    /* Go Kart */
    public static final ComponentModel GO_KART_BODY = LOADER.create("item/go_kart_base");
    public static final ComponentModel KEYHOLE_GK = LOADER.create("item/key_hole_gk");
    public static final ComponentModel GO_KART_STEERING_WHEEL = LOADER.create("item/steering_wheel");

    /* Sports Plane */
    public static final ComponentModel SPORTS_PLANE_BODY = LOADER.create("item/sports_plane_body");
    public static final ComponentModel SPORTS_PLANE_WINGS = LOADER.create("item/sports_plane_wings");
    public static final ComponentModel SPORTS_PLANE_SEAT = LOADER.create("item/sports_plane_seat");
    public static final ComponentModel SPORTS_PLANE_PROPELLER = LOADER.create("item/sports_plane_propeller");
    public static final ComponentModel SPORTS_PLANE_LEFT_AILERON = LOADER.create("item/sports_plane/cosmetics/left_aileron");
    public static final ComponentModel SPORTS_PLANE_RIGHT_AILERON = LOADER.create("item/sports_plane/cosmetics/right_aileron");
    public static final ComponentModel SPORTS_PLANE_ELEVATOR = LOADER.create("item/sports_plane/cosmetics/elevator");
    public static final ComponentModel SPORTS_PLANE_JOYSTICK = LOADER.create("item/sports_plane/cosmetics/joystick");
    public static final ComponentModel SPORTS_PLANE_BACK_RIGHT_LEG = LOADER.create("item/sports_plane_back_right_leg");
    public static final ComponentModel SPORTS_PLANE_BACK_LEFT_LEG = LOADER.create("item/sports_plane_back_left_leg");



    /* FEULLING HITBOX WORKAROUND*/
    public static final ComponentModel DIRT_BIKE_FUEL_DOOR_CLOSED = LOADER.create("item/dirt_bike_fuel_door_closed");
    public static final ComponentModel ALUMINUM_BOAT_FUEL_DOOR_CLOSED = LOADER.create("item/aluminum_boat_fuel_door");
    public static final ComponentModel ATV_FUEL_DOOR_CLOSED = LOADER.create("item/atv_fuel_door_closed");
    public static final ComponentModel BUMPER_CAR_FUEL_DOOR_CLOSED = LOADER.create("item/bumper_car_fuel_door_closed");
    public static final ComponentModel HELI_FD = LOADER.create("item/heli_fd");
    public static final ComponentModel FUEL_HITBOX_WORKAROUND = LOADER.create("item/fuel_hitbox_workaround");
    public static final ComponentModel DUNE_BUGGY_FUEL_DOOR_CLOSED = LOADER.create("item/dune_buggy_fuel_door_closed");
    public static final ComponentModel GOLF_CART_FUEL_DOOR_CLOSED = LOADER.create("item/golf_cart_fuel_door_closed");
    public static final ComponentModel GO_KART_FUEL_DOOR_CLOSED =  LOADER.create("item/go_kart_fuel_door_closed");
    public static final ComponentModel JET_SKI_FUEL_DOOR_CLOSED = LOADER.create("item/jet_ski_fuel_door_closed");
    public static final ComponentModel LAWN_MOWER_FUEL_DOOR_CLOSED = LOADER.create("item/lawn_mower_fuel_door_closed");
    public static final ComponentModel MINI_BIKE_FUEL_DOOR_CLOSED = LOADER.create("item/mini_bike_fuel_door_closed");
    public static final ComponentModel MINI_BUS_FUEL_DOOR_CLOSED = LOADER.create("item/minibus_fuel_door_closed");
    public static final ComponentModel MOPED_FUEL_DOOR_CLOSED = LOADER.create("item/moped_fuel_door_closed");
    public static final ComponentModel OFF_ROADER_FUEL_DOOR_CLOSED = LOADER.create("item/off_roader_fuel_door_closed");
    public static final ComponentModel QUAD_BIKE_FUEL_DOOR_CLOSED = LOADER.create("item/quad_bike_fuel_door_closed");
    public static final ComponentModel SMART_CAR_FUEL_DOOR_CLOSED = LOADER.create("item/smart_car_fuel_door_closed");
    public static final ComponentModel SOFACOPTER_FUEL_DOOR_CLOSED = LOADER.create("item/sofacopter_fuel_door_closed");
    public static final ComponentModel SPEED_BOAT_FUEL_DOOR_CLOSED = LOADER.create("item/speed_boat_fuel_door_closed");
    public static final ComponentModel SPORTS_CAR_FUEL_DOOR_CLOSED = LOADER.create("item/sports_car_fuel_door_closed");
    public static final ComponentModel SPORTS_PLANE_FUEL_DOOR_CLOSED = LOADER.create("item/sports_plane_fuel_door_closed");
    public static final ComponentModel TRACTOR_FUEL_DOOR_CLOSED = LOADER.create("item/tractor_fuel_door_closed");
    /* Helicopter */
    public static final ComponentModel HELICOPTER_BODY = LOADER.create("item/helicopter_base");
    public static final ComponentModel HELICOPTER_BLADES = LOADER.create("item/helicopter_blades");
    public static final ComponentModel HELICOPTER_SEAT = LOADER.create("item/helicopter_seat");
    public static final ComponentModel HELICOPTER_JOYSTICK = LOADER.create("item/helicopter_joystick");
    public static final ComponentModel HELICOPTER_TAIL_ROTOR = LOADER.create("item/helicopter_tail_rotor");

    public static final ComponentModel JET_SKI_BODY = LOADER.create("item/jet_ski_body");
    public static final ComponentModel LAWN_MOWER_BODY = LOADER.create("item/lawn_mower_body");
    public static final ComponentModel SPORTS_PLANE = LOADER.create("item/sports_plane_body");
    public static final ComponentModel SPORTS_PLANE_RIGHT_WING = LOADER.create("item/sports_plane_right_wing");
    public static final ComponentModel SPORTS_PLANE_LEFT_WING = LOADER.create("item/sports_plane_left_wing");
    public static final ComponentModel SPORTS_PLANE_WHEEL_COVER = LOADER.create("item/sports_plane_wheel_cover");
    public static final ComponentModel SPORTS_PLANE_FRONT_LEG = LOADER.create("item/sports_plane_front_leg");
    public static final ComponentModel GOLF_CART_BODY = LOADER.create("item/golf_cart_body");
    public static final ComponentModel OFF_ROADER_BODY = LOADER.create("item/off_roader_body");
    public static final ComponentModel TRACTOR = LOADER.create("item/tractor_body");
    public static final ComponentModel VEHICLE_TRAILER = LOADER.create("item/trailer_body");
    public static final ComponentModel STORAGE_TRAILER = LOADER.create("item/trailer_chest_body");
    public static final ComponentModel SEEDER_TRAILER = LOADER.create("item/trailer_seeder_body");
    public static final ComponentModel FERTILIZER_TRAILER = LOADER.create("item/trailer_fertilizer_body");
    public static final ComponentModel FLUID_TRAILER = LOADER.create("item/trailer_fluid_body");

    public static final ComponentModel VEHICLE_CRATE_SIDE = LOADER.create("item/vehicle_crate_panel_side");
    public static final ComponentModel VEHICLE_CRATE_TOP = LOADER.create("item/vehicle_crate_panel_top");
    public static final ComponentModel JACK_PISTON_HEAD = LOADER.create("item/jack_piston_head");
    public static final ComponentModel SEED_SPIKER = LOADER.create("item/seed_spiker");
    public static final ComponentModel NOZZLE = LOADER.create("item/nozzle");
    public static final ComponentModel TOW_BAR = LOADER.create("item/tow_bar");
    public static final ComponentModel BIG_TOW_BAR = LOADER.create("item/big_tow_bar");
    public static final ComponentModel FUEL_DOOR_CLOSED = LOADER.create("item/fuel_door_closed");
    public static final ComponentModel FUEL_PORT_BODY = LOADER.create("item/fuel_port_body");
    public static final ComponentModel FUEL_DOOR_OPEN = LOADER.create("item/fuel_door_open");
    public static final ComponentModel SMALL_FUEL_DOOR_CLOSED = LOADER.create("item/small_fuel_door_closed");
    public static final ComponentModel SMALL_FUEL_DOOR_OPEN = LOADER.create("item/small_fuel_door_open");
    public static final ComponentModel KEY_HOLE = LOADER.create("item/key_hole");
    public static final ComponentModel KEY_HOLE_BC = LOADER.create("item/key_hole_bc");
    public static final ComponentModel SOFA_HELICOPTER_ARM = LOADER.create("item/sofa_helicopter_arm");
    public static final ComponentModel SOFA_HELICOPTER_SKID = LOADER.create("item/sofa_helicopter_skid");
    public static final ComponentModel ALUMINUM_BOAT_BODY = LOADER.create("item/aluminum_boat_body");
    public static final ComponentModel MINI_BIKE_BODY = LOADER.create("item/mini_bike_body");
    public static final ComponentModel MINI_BIKE_HANDLES = LOADER.create("item/mini_bike_handles");
    public static final ComponentModel SMART_CAR_BODY = LOADER.create("item/smart_car_body");
    public static final ComponentModel SMART_CAR_STEERING_WHEEL = LOADER.create("item/steering_wheel");
    public static final ComponentModel SPEED_BOAT_BODY = LOADER.create("item/speed_boat_body");

    // Special use to reference existing models
    public static final ComponentModel RED_SOFA = new ComponentModel(new ModelResourceLocation(new ResourceLocation("refurbished_furniture", "red_sofa"), "inventory"));
    public static final ComponentModel CEILING_FAN = new ComponentModel(new ModelResourceLocation(new ResourceLocation("refurbished_furniture", "dark_oak_light_ceiling_fan"), "inventory"));

    public static final ComponentModel BATH = new ComponentModel(new ModelResourceLocation(new ResourceLocation("refurbished_furniture", "red_bath"), "inventory"));
}
