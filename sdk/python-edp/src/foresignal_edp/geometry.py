"""GeoJSON geometry builders and the EDP §3.3 geometry check.

Accepted types: Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon (no
GeometryCollection); WGS84 longitude -180..180 and latitude -90..90; two or three finite ordinates
(the third is altitude in meters); closed linear rings of at least four positions; at most 10,000
vertices; and a type compatible with the stream's declared ``geometry``.
"""

from __future__ import annotations

import math
from typing import Any, Iterable, Sequence

from .errors import SchemaError
from .protocol import GEOMETRY_TYPES, MAX_VERTICES

Position = Sequence[float]

_COMPATIBLE: dict[str, frozenset[str]] = {
    "Point": frozenset({"Point", "MultiPoint"}),
    "LineString": frozenset({"LineString", "MultiLineString"}),
    "Polygon": frozenset({"Polygon", "MultiPolygon"}),
    "MultiPolygon": frozenset({"Polygon", "MultiPolygon"}),
    "any": frozenset(GEOMETRY_TYPES),
    "none": frozenset(),
}


def _position(position: Position) -> list[float]:
    return list(position)


def point(lon: float, lat: float, alt: float | None = None) -> dict[str, Any]:
    """A GeoJSON ``Point`` at ``lon``, ``lat`` (and ``alt`` meters when given)."""
    coordinates: list[float] = [lon, lat] if alt is None else [lon, lat, alt]
    return {"type": "Point", "coordinates": coordinates}


def multi_point(positions: Iterable[Position]) -> dict[str, Any]:
    """A GeoJSON ``MultiPoint``."""
    return {"type": "MultiPoint", "coordinates": [_position(p) for p in positions]}


def line_string(positions: Iterable[Position]) -> dict[str, Any]:
    """A GeoJSON ``LineString`` (at least two positions)."""
    return {"type": "LineString", "coordinates": [_position(p) for p in positions]}


def multi_line_string(lines: Iterable[Iterable[Position]]) -> dict[str, Any]:
    """A GeoJSON ``MultiLineString``."""
    return {"type": "MultiLineString", "coordinates": [[_position(p) for p in line] for line in lines]}


def _ring(ring: Iterable[Position], close: bool) -> list[list[float]]:
    positions = [_position(p) for p in ring]
    if close and positions and positions[0][:2] != positions[-1][:2]:
        positions.append(list(positions[0]))
    return positions


def polygon(rings: Iterable[Iterable[Position]], *, close: bool = True) -> dict[str, Any]:
    """A GeoJSON ``Polygon``; each ring is closed by repeating its first position unless ``close`` is false."""
    return {"type": "Polygon", "coordinates": [_ring(r, close) for r in rings]}


def multi_polygon(polygons: Iterable[Iterable[Iterable[Position]]], *, close: bool = True) -> dict[str, Any]:
    """A GeoJSON ``MultiPolygon``; rings are closed as in :func:`polygon`."""
    return {"type": "MultiPolygon", "coordinates": [[_ring(r, close) for r in p] for p in polygons]}


def bbox_polygon(min_lon: float, min_lat: float, max_lon: float, max_lat: float) -> dict[str, Any]:
    """A rectangular ``Polygon`` (counterclockwise exterior ring), handy for ``coverage.area``."""
    return polygon([[(min_lon, min_lat), (max_lon, min_lat), (max_lon, max_lat), (min_lon, max_lat)]])


class _Counter:
    def __init__(self) -> None:
        self.vertices = 0


def _is_number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and math.isfinite(value)


def _check_position(position: Any, path: str, errors: list[SchemaError], counter: _Counter) -> None:
    counter.vertices += 1
    if not isinstance(position, list) or not 2 <= len(position) <= 3:
        errors.append(SchemaError(path, "a position has two or three numbers"))
        return
    if not all(_is_number(v) for v in position):
        errors.append(SchemaError(path, "ordinates must be finite numbers"))
        return
    lon, lat = position[0], position[1]
    if lon < -180 or lon > 180:
        errors.append(SchemaError(path + "/0", "longitude must be between -180 and 180"))
    if lat < -90 or lat > 90:
        errors.append(SchemaError(path + "/1", "latitude must be between -90 and 90"))


def _check_positions(positions: list[Any], path: str, errors: list[SchemaError], counter: _Counter) -> None:
    for i, position in enumerate(positions):
        _check_position(position, f"{path}/{i}", errors, counter)
        if counter.vertices > MAX_VERTICES:
            return


def _check_line(line: list[Any], path: str, errors: list[SchemaError], counter: _Counter) -> None:
    if len(line) < 2:
        errors.append(SchemaError(path, "a line needs at least two positions"))
    _check_positions(line, path, errors, counter)


def _same_position(a: Any, b: Any) -> bool:
    if not isinstance(a, list) or not isinstance(b, list) or len(a) < 2 or len(b) < 2:
        return False
    return float(a[0]) == float(b[0]) and float(a[1]) == float(b[1])


def _check_polygon(rings: list[Any], path: str, errors: list[SchemaError], counter: _Counter) -> None:
    if not rings:
        errors.append(SchemaError(path, "a polygon needs at least one ring"))
    for r, ring in enumerate(rings):
        ring_path = f"{path}/{r}"
        if not isinstance(ring, list):
            errors.append(SchemaError(ring_path, "must be an array"))
            continue
        if len(ring) < 4:
            errors.append(SchemaError(ring_path, "a linear ring needs at least four positions"))
        before = len(errors)
        _check_positions(ring, ring_path, errors, counter)
        if len(errors) == before and len(ring) >= 2 and not _same_position(ring[0], ring[-1]):
            errors.append(SchemaError(ring_path, "a linear ring must be closed (first and last positions equal)"))


def _each(array: list[Any], path: str, errors: list[SchemaError], part: Any) -> None:
    if not array:
        errors.append(SchemaError(path, "must not be empty"))
    for i, child in enumerate(array):
        if not isinstance(child, list):
            errors.append(SchemaError(f"{path}/{i}", "must be an array"))
            continue
        part(child, f"{path}/{i}")


def validate_geometry(geometry: Any, declared: str | None = None, path: str = "/geometry") -> list[SchemaError]:
    """Returns the problems of ``geometry`` for a stream declaring ``declared`` (``any`` when ``None``).

    ``None`` means "no geometry" and is always acceptable here (whether a stream needs one is not a
    geometry rule).
    """
    errors: list[SchemaError] = []
    if geometry is None:
        return errors
    allowed_key = declared or "any"
    allowed = _COMPATIBLE.get(allowed_key, _COMPATIBLE["any"])
    if not isinstance(geometry, dict):
        errors.append(SchemaError(path, "must be a GeoJSON geometry object"))
        return errors
    kind = geometry.get("type")
    if not isinstance(kind, str) or kind not in GEOMETRY_TYPES:
        errors.append(SchemaError(path + "/type",
                                  "must be Point, MultiPoint, LineString, MultiLineString, Polygon or MultiPolygon"))
        return errors
    if kind not in allowed:
        errors.append(SchemaError(path + "/type", f"{kind} does not match the stream's declared geometry {allowed_key}"))
        return errors
    coordinates = geometry.get("coordinates")
    coords = path + "/coordinates"
    if not isinstance(coordinates, list):
        errors.append(SchemaError(coords, "must be an array"))
        return errors
    counter = _Counter()
    if kind == "Point":
        _check_position(coordinates, coords, errors, counter)
    elif kind == "MultiPoint":
        _each(coordinates, coords, errors, lambda c, p: _check_position(c, p, errors, counter))
    elif kind == "LineString":
        _check_line(coordinates, coords, errors, counter)
    elif kind == "MultiLineString":
        _each(coordinates, coords, errors, lambda c, p: _check_line(c, p, errors, counter))
    elif kind == "Polygon":
        _check_polygon(coordinates, coords, errors, counter)
    else:
        _each(coordinates, coords, errors, lambda c, p: _check_polygon(c, p, errors, counter))
    if counter.vertices > MAX_VERTICES:
        errors.append(SchemaError(coords, f"has {counter.vertices} vertices; the maximum is {MAX_VERTICES}"))
    return errors
