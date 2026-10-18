import datetime
from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_tile_agg import GetTileAgg
from ...models.get_tile_licence_policy_item import GetTileLicencePolicyItem
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    layer: str,
    z: int,
    x: int,
    y: int,
    *,
    agg: GetTileAgg | Unset = GetTileAgg.AUTO,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    licence_policy: list[GetTileLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    json_agg: str | Unset = UNSET
    if not isinstance(agg, Unset):
        json_agg = agg.value

    params["agg"] = json_agg

    params["ticket"] = ticket

    json_place: list[str] | Unset = UNSET
    if not isinstance(place, Unset):
        json_place = place

    params["place"] = json_place

    params["radiusKm"] = radius_km

    params["view"] = view

    json_in_watchlist: str | Unset = UNSET
    if not isinstance(in_watchlist, Unset):
        json_in_watchlist = str(in_watchlist)
    params["inWatchlist"] = json_in_watchlist

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/tiles/{layer}/{z}/{x}/{y}.mvt".format(
            layer=quote(str(layer), safe=""),
            z=quote(str(z), safe=""),
            x=quote(str(x), safe=""),
            y=quote(str(y), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

        return response_200

    if response.status_code == 301:
        response_301 = cast(Any, None)
        return response_301

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    layer: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    agg: GetTileAgg | Unset = GetTileAgg.AUTO,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    licence_policy: list[GetTileLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Vector tile of current records

     Mapbox Vector Tile of current live state (record_live ⋈ record_head): geometry and observedAt come
    from record_live, so a live position shows before any new version is persisted. `layer` is a source
    id (wins) or a kind declared by a visible source; anything else is 404 UNKNOWN_LAYER. Raw features
    carry `source`, `externalId`, `kind`, `title` (record_head.title), `observedAt` (epoch seconds,
    number), `version` (number), `live` (boolean, record_live.dirty) plus the layer sources' valid
    `tileProperties` (names matching `^[A-Za-z0-9_]{1,64}$`) read from the merged payload with their
    native JSON types; null values are omitted. Raw tiles are capped at foresignal.tiles.maxFeatures
    (default 50,000) newest-first, with `Foresignal-Tile-Truncated: true` when the cap was hit.
    Aggregated tiles (`agg=grid|hex`, or `auto` below the layer's aggBelowZoom, which is the lowest
    value its visible sources declare) carry one polygon per non-empty cell with `count`,
    `latestObservedAt` (epoch seconds), `kinds` (a JSON string, kind → count), `agg` (`grid` or `hex`)
    and `maxRank` only when every source of the layer declares a numeric rankField. Cells are built in
    EPSG:3857 with foresignal.tiles.cellsPerTile (default 32) cells across a tile; grid cells line up
    with tile edges. Each record is reduced to ST_PointOnSurface (a polygon counts once) and counted in
    exactly one tile (half-open tile envelope) and one cell; a hex cell that straddles a tile edge is
    drawn by both tiles, each counting the records on its own side. Private sources never appear in
    features or counts for anonymous callers. An empty tile is 200 with an empty body. With `asOf` (037)
    the tile shows the layer as known at that instant (transaction axis, record_version): per entity the
    newest version with seq in [L, S(asOf)] that is not a tombstone, still inside the tile and valid at
    asOf (an entity that moved out is not drawn at its old position); raw features carry `source`,
    `externalId`, `kind`, `title` (from the version payload), `observedAt`, `version` and the tile
    properties, without `live`; the same cap and aggregation apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        z (int):
        x (int):
        y (int):
        agg (GetTileAgg | Unset):  Default: GetTileAgg.AUTO.
        ticket (str | Unset):
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        as_of (datetime.datetime | Unset):
        licence_policy (list[GetTileLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        layer=layer,
        z=z,
        x=x,
        y=y,
        agg=agg,
        ticket=ticket,
        place=place,
        radius_km=radius_km,
        view=view,
        in_watchlist=in_watchlist,
        as_of=as_of,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    layer: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    agg: GetTileAgg | Unset = GetTileAgg.AUTO,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    licence_policy: list[GetTileLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Vector tile of current records

     Mapbox Vector Tile of current live state (record_live ⋈ record_head): geometry and observedAt come
    from record_live, so a live position shows before any new version is persisted. `layer` is a source
    id (wins) or a kind declared by a visible source; anything else is 404 UNKNOWN_LAYER. Raw features
    carry `source`, `externalId`, `kind`, `title` (record_head.title), `observedAt` (epoch seconds,
    number), `version` (number), `live` (boolean, record_live.dirty) plus the layer sources' valid
    `tileProperties` (names matching `^[A-Za-z0-9_]{1,64}$`) read from the merged payload with their
    native JSON types; null values are omitted. Raw tiles are capped at foresignal.tiles.maxFeatures
    (default 50,000) newest-first, with `Foresignal-Tile-Truncated: true` when the cap was hit.
    Aggregated tiles (`agg=grid|hex`, or `auto` below the layer's aggBelowZoom, which is the lowest
    value its visible sources declare) carry one polygon per non-empty cell with `count`,
    `latestObservedAt` (epoch seconds), `kinds` (a JSON string, kind → count), `agg` (`grid` or `hex`)
    and `maxRank` only when every source of the layer declares a numeric rankField. Cells are built in
    EPSG:3857 with foresignal.tiles.cellsPerTile (default 32) cells across a tile; grid cells line up
    with tile edges. Each record is reduced to ST_PointOnSurface (a polygon counts once) and counted in
    exactly one tile (half-open tile envelope) and one cell; a hex cell that straddles a tile edge is
    drawn by both tiles, each counting the records on its own side. Private sources never appear in
    features or counts for anonymous callers. An empty tile is 200 with an empty body. With `asOf` (037)
    the tile shows the layer as known at that instant (transaction axis, record_version): per entity the
    newest version with seq in [L, S(asOf)] that is not a tombstone, still inside the tile and valid at
    asOf (an entity that moved out is not drawn at its old position); raw features carry `source`,
    `externalId`, `kind`, `title` (from the version payload), `observedAt`, `version` and the tile
    properties, without `live`; the same cap and aggregation apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        z (int):
        x (int):
        y (int):
        agg (GetTileAgg | Unset):  Default: GetTileAgg.AUTO.
        ticket (str | Unset):
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        as_of (datetime.datetime | Unset):
        licence_policy (list[GetTileLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        layer=layer,
        z=z,
        x=x,
        y=y,
        client=client,
        agg=agg,
        ticket=ticket,
        place=place,
        radius_km=radius_km,
        view=view,
        in_watchlist=in_watchlist,
        as_of=as_of,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    layer: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    agg: GetTileAgg | Unset = GetTileAgg.AUTO,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    licence_policy: list[GetTileLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Vector tile of current records

     Mapbox Vector Tile of current live state (record_live ⋈ record_head): geometry and observedAt come
    from record_live, so a live position shows before any new version is persisted. `layer` is a source
    id (wins) or a kind declared by a visible source; anything else is 404 UNKNOWN_LAYER. Raw features
    carry `source`, `externalId`, `kind`, `title` (record_head.title), `observedAt` (epoch seconds,
    number), `version` (number), `live` (boolean, record_live.dirty) plus the layer sources' valid
    `tileProperties` (names matching `^[A-Za-z0-9_]{1,64}$`) read from the merged payload with their
    native JSON types; null values are omitted. Raw tiles are capped at foresignal.tiles.maxFeatures
    (default 50,000) newest-first, with `Foresignal-Tile-Truncated: true` when the cap was hit.
    Aggregated tiles (`agg=grid|hex`, or `auto` below the layer's aggBelowZoom, which is the lowest
    value its visible sources declare) carry one polygon per non-empty cell with `count`,
    `latestObservedAt` (epoch seconds), `kinds` (a JSON string, kind → count), `agg` (`grid` or `hex`)
    and `maxRank` only when every source of the layer declares a numeric rankField. Cells are built in
    EPSG:3857 with foresignal.tiles.cellsPerTile (default 32) cells across a tile; grid cells line up
    with tile edges. Each record is reduced to ST_PointOnSurface (a polygon counts once) and counted in
    exactly one tile (half-open tile envelope) and one cell; a hex cell that straddles a tile edge is
    drawn by both tiles, each counting the records on its own side. Private sources never appear in
    features or counts for anonymous callers. An empty tile is 200 with an empty body. With `asOf` (037)
    the tile shows the layer as known at that instant (transaction axis, record_version): per entity the
    newest version with seq in [L, S(asOf)] that is not a tombstone, still inside the tile and valid at
    asOf (an entity that moved out is not drawn at its old position); raw features carry `source`,
    `externalId`, `kind`, `title` (from the version payload), `observedAt`, `version` and the tile
    properties, without `live`; the same cap and aggregation apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        z (int):
        x (int):
        y (int):
        agg (GetTileAgg | Unset):  Default: GetTileAgg.AUTO.
        ticket (str | Unset):
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        as_of (datetime.datetime | Unset):
        licence_policy (list[GetTileLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        layer=layer,
        z=z,
        x=x,
        y=y,
        agg=agg,
        ticket=ticket,
        place=place,
        radius_km=radius_km,
        view=view,
        in_watchlist=in_watchlist,
        as_of=as_of,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    layer: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    agg: GetTileAgg | Unset = GetTileAgg.AUTO,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    licence_policy: list[GetTileLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Vector tile of current records

     Mapbox Vector Tile of current live state (record_live ⋈ record_head): geometry and observedAt come
    from record_live, so a live position shows before any new version is persisted. `layer` is a source
    id (wins) or a kind declared by a visible source; anything else is 404 UNKNOWN_LAYER. Raw features
    carry `source`, `externalId`, `kind`, `title` (record_head.title), `observedAt` (epoch seconds,
    number), `version` (number), `live` (boolean, record_live.dirty) plus the layer sources' valid
    `tileProperties` (names matching `^[A-Za-z0-9_]{1,64}$`) read from the merged payload with their
    native JSON types; null values are omitted. Raw tiles are capped at foresignal.tiles.maxFeatures
    (default 50,000) newest-first, with `Foresignal-Tile-Truncated: true` when the cap was hit.
    Aggregated tiles (`agg=grid|hex`, or `auto` below the layer's aggBelowZoom, which is the lowest
    value its visible sources declare) carry one polygon per non-empty cell with `count`,
    `latestObservedAt` (epoch seconds), `kinds` (a JSON string, kind → count), `agg` (`grid` or `hex`)
    and `maxRank` only when every source of the layer declares a numeric rankField. Cells are built in
    EPSG:3857 with foresignal.tiles.cellsPerTile (default 32) cells across a tile; grid cells line up
    with tile edges. Each record is reduced to ST_PointOnSurface (a polygon counts once) and counted in
    exactly one tile (half-open tile envelope) and one cell; a hex cell that straddles a tile edge is
    drawn by both tiles, each counting the records on its own side. Private sources never appear in
    features or counts for anonymous callers. An empty tile is 200 with an empty body. With `asOf` (037)
    the tile shows the layer as known at that instant (transaction axis, record_version): per entity the
    newest version with seq in [L, S(asOf)] that is not a tombstone, still inside the tile and valid at
    asOf (an entity that moved out is not drawn at its old position); raw features carry `source`,
    `externalId`, `kind`, `title` (from the version payload), `observedAt`, `version` and the tile
    properties, without `live`; the same cap and aggregation apply.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        z (int):
        x (int):
        y (int):
        agg (GetTileAgg | Unset):  Default: GetTileAgg.AUTO.
        ticket (str | Unset):
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        as_of (datetime.datetime | Unset):
        licence_policy (list[GetTileLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            layer=layer,
            z=z,
            x=x,
            y=y,
            client=client,
            agg=agg,
            ticket=ticket,
            place=place,
            radius_km=radius_km,
            view=view,
            in_watchlist=in_watchlist,
            as_of=as_of,
            licence_policy=licence_policy,
            if_none_match=if_none_match,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
