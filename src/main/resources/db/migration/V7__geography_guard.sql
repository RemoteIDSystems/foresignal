-- Distance on geography for stored geometries, safe for shapes PostGIS refuses to cast.
--
-- A geometry with an antipodal edge (two vertices 180 degrees apart, such as a world-covering raster frame,
-- POLYGON((-180 -90,180 -90,180 90,-180 90,-180 -90))) makes geom::geography fail with "Antipodal (180 degrees
-- long) edge detected!", and one such row fails the whole query. Raster frames are densified at ingest
-- (RasterFrameRecords); these functions are the query-side guard for rows stored before that and for any
-- other source.
--
-- Geography also cannot model a polygon larger than a hemisphere, so a covering shape counts as distance 0
-- by planar containment: a global forecast frame is near every point, as it is in a bbox query.

-- An antipodal edge needs a 180-degree span, so only shapes that wide are tried; the cast that fails is retried
-- on the shape densified to 10-degree edges. Shapes that cast cleanly keep their great-circle edges.
create function fs_geog_wide(g geometry) returns geography
    language plpgsql immutable strict parallel safe as $$
begin
    return g::geography;
exception when others then
    return ST_Segmentize(ST_MakeValid(g), 10)::geography;
end
$$;

-- g as geography: the plain cast, or fs_geog_wide for a shape spanning 180 degrees or more.
create function fs_geog(g geometry) returns geography
    language sql immutable parallel safe as $$
    select case when ST_XMax(g) - ST_XMin(g) < 180 and ST_YMax(g) - ST_YMin(g) < 180
        then g::geography else fs_geog_wide(g) end
$$;

-- True when g lies within meters of p: g contains or touches p, or its geography is that close.
create function fs_dwithin(g geometry, p geometry, meters double precision) returns boolean
    language sql immutable parallel safe as $$
    select ST_Intersects(g, p) or ST_DWithin(fs_geog(g), fs_geog(p), meters)
$$;

-- The distance in meters from g to p on geography, 0 when they intersect.
create function fs_distance(g geometry, p geometry) returns double precision
    language sql immutable parallel safe as $$
    select case when ST_Intersects(g, p) then 0 else ST_Distance(fs_geog(g), fs_geog(p)) end
$$;
