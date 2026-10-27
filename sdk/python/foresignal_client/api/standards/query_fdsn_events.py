import datetime
from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.query_fdsn_events_format import QueryFdsnEventsFormat
from ...models.query_fdsn_events_nodata import QueryFdsnEventsNodata
from ...models.query_fdsn_events_orderby import QueryFdsnEventsOrderby
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    starttime: datetime.datetime | Unset = UNSET,
    endtime: datetime.datetime | Unset = UNSET,
    minlatitude: float | Unset = UNSET,
    maxlatitude: float | Unset = UNSET,
    minlongitude: float | Unset = UNSET,
    maxlongitude: float | Unset = UNSET,
    latitude: float | Unset = UNSET,
    longitude: float | Unset = UNSET,
    minradius: float | Unset = UNSET,
    maxradius: float | Unset = UNSET,
    minmagnitude: float | Unset = UNSET,
    maxmagnitude: float | Unset = UNSET,
    orderby: QueryFdsnEventsOrderby | Unset = QueryFdsnEventsOrderby.TIME,
    limit: int | Unset = UNSET,
    format_: QueryFdsnEventsFormat | Unset = QueryFdsnEventsFormat.XML,
    nodata: QueryFdsnEventsNodata | Unset = QueryFdsnEventsNodata.VALUE_204,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_starttime: str | Unset = UNSET
    if not isinstance(starttime, Unset):
        json_starttime = starttime.isoformat()
    params["starttime"] = json_starttime

    json_endtime: str | Unset = UNSET
    if not isinstance(endtime, Unset):
        json_endtime = endtime.isoformat()
    params["endtime"] = json_endtime

    params["minlatitude"] = minlatitude

    params["maxlatitude"] = maxlatitude

    params["minlongitude"] = minlongitude

    params["maxlongitude"] = maxlongitude

    params["latitude"] = latitude

    params["longitude"] = longitude

    params["minradius"] = minradius

    params["maxradius"] = maxradius

    params["minmagnitude"] = minmagnitude

    params["maxmagnitude"] = maxmagnitude

    json_orderby: str | Unset = UNSET
    if not isinstance(orderby, Unset):
        json_orderby = orderby.value

    params["orderby"] = json_orderby

    params["limit"] = limit

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    json_nodata: int | Unset = UNSET
    if not isinstance(nodata, Unset):
        json_nodata = nodata.value

    params["nodata"] = json_nodata

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/fdsnws/event/1/query",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

    if response.status_code == 400:
        response_400 = response.text
        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = response.text
        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    starttime: datetime.datetime | Unset = UNSET,
    endtime: datetime.datetime | Unset = UNSET,
    minlatitude: float | Unset = UNSET,
    maxlatitude: float | Unset = UNSET,
    minlongitude: float | Unset = UNSET,
    maxlongitude: float | Unset = UNSET,
    latitude: float | Unset = UNSET,
    longitude: float | Unset = UNSET,
    minradius: float | Unset = UNSET,
    maxradius: float | Unset = UNSET,
    minmagnitude: float | Unset = UNSET,
    maxmagnitude: float | Unset = UNSET,
    orderby: QueryFdsnEventsOrderby | Unset = QueryFdsnEventsOrderby.TIME,
    limit: int | Unset = UNSET,
    format_: QueryFdsnEventsFormat | Unset = QueryFdsnEventsFormat.XML,
    nodata: QueryFdsnEventsNodata | Unset = QueryFdsnEventsNodata.VALUE_204,
) -> Response[Any | Problem | str]:
    """FDSN event query

     fdsnws-event 1.2 over `earthquake` records: QuakeML 1.2 BED (`smi:foresignal/{source}/{externalId}`
    publicIDs) or the pipe-separated text format. `204` when no events match (FDSN convention;
    `nodata=404` selects 404); errors use FDSN plain-text bodies. Long and short parameter names are
    accepted (`start`, `minlat`, `minmag`, …); unsupported parameters are a 400. Depth is in meters in
    QuakeML and in km in the text format.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        starttime (datetime.datetime | Unset):
        endtime (datetime.datetime | Unset):
        minlatitude (float | Unset):
        maxlatitude (float | Unset):
        minlongitude (float | Unset):
        maxlongitude (float | Unset):
        latitude (float | Unset):
        longitude (float | Unset):
        minradius (float | Unset):
        maxradius (float | Unset):
        minmagnitude (float | Unset):
        maxmagnitude (float | Unset):
        orderby (QueryFdsnEventsOrderby | Unset):  Default: QueryFdsnEventsOrderby.TIME.
        limit (int | Unset):
        format_ (QueryFdsnEventsFormat | Unset):  Default: QueryFdsnEventsFormat.XML.
        nodata (QueryFdsnEventsNodata | Unset):  Default: QueryFdsnEventsNodata.VALUE_204.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        starttime=starttime,
        endtime=endtime,
        minlatitude=minlatitude,
        maxlatitude=maxlatitude,
        minlongitude=minlongitude,
        maxlongitude=maxlongitude,
        latitude=latitude,
        longitude=longitude,
        minradius=minradius,
        maxradius=maxradius,
        minmagnitude=minmagnitude,
        maxmagnitude=maxmagnitude,
        orderby=orderby,
        limit=limit,
        format_=format_,
        nodata=nodata,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    starttime: datetime.datetime | Unset = UNSET,
    endtime: datetime.datetime | Unset = UNSET,
    minlatitude: float | Unset = UNSET,
    maxlatitude: float | Unset = UNSET,
    minlongitude: float | Unset = UNSET,
    maxlongitude: float | Unset = UNSET,
    latitude: float | Unset = UNSET,
    longitude: float | Unset = UNSET,
    minradius: float | Unset = UNSET,
    maxradius: float | Unset = UNSET,
    minmagnitude: float | Unset = UNSET,
    maxmagnitude: float | Unset = UNSET,
    orderby: QueryFdsnEventsOrderby | Unset = QueryFdsnEventsOrderby.TIME,
    limit: int | Unset = UNSET,
    format_: QueryFdsnEventsFormat | Unset = QueryFdsnEventsFormat.XML,
    nodata: QueryFdsnEventsNodata | Unset = QueryFdsnEventsNodata.VALUE_204,
) -> Any | Problem | str | None:
    """FDSN event query

     fdsnws-event 1.2 over `earthquake` records: QuakeML 1.2 BED (`smi:foresignal/{source}/{externalId}`
    publicIDs) or the pipe-separated text format. `204` when no events match (FDSN convention;
    `nodata=404` selects 404); errors use FDSN plain-text bodies. Long and short parameter names are
    accepted (`start`, `minlat`, `minmag`, …); unsupported parameters are a 400. Depth is in meters in
    QuakeML and in km in the text format.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        starttime (datetime.datetime | Unset):
        endtime (datetime.datetime | Unset):
        minlatitude (float | Unset):
        maxlatitude (float | Unset):
        minlongitude (float | Unset):
        maxlongitude (float | Unset):
        latitude (float | Unset):
        longitude (float | Unset):
        minradius (float | Unset):
        maxradius (float | Unset):
        minmagnitude (float | Unset):
        maxmagnitude (float | Unset):
        orderby (QueryFdsnEventsOrderby | Unset):  Default: QueryFdsnEventsOrderby.TIME.
        limit (int | Unset):
        format_ (QueryFdsnEventsFormat | Unset):  Default: QueryFdsnEventsFormat.XML.
        nodata (QueryFdsnEventsNodata | Unset):  Default: QueryFdsnEventsNodata.VALUE_204.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return sync_detailed(
        client=client,
        starttime=starttime,
        endtime=endtime,
        minlatitude=minlatitude,
        maxlatitude=maxlatitude,
        minlongitude=minlongitude,
        maxlongitude=maxlongitude,
        latitude=latitude,
        longitude=longitude,
        minradius=minradius,
        maxradius=maxradius,
        minmagnitude=minmagnitude,
        maxmagnitude=maxmagnitude,
        orderby=orderby,
        limit=limit,
        format_=format_,
        nodata=nodata,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    starttime: datetime.datetime | Unset = UNSET,
    endtime: datetime.datetime | Unset = UNSET,
    minlatitude: float | Unset = UNSET,
    maxlatitude: float | Unset = UNSET,
    minlongitude: float | Unset = UNSET,
    maxlongitude: float | Unset = UNSET,
    latitude: float | Unset = UNSET,
    longitude: float | Unset = UNSET,
    minradius: float | Unset = UNSET,
    maxradius: float | Unset = UNSET,
    minmagnitude: float | Unset = UNSET,
    maxmagnitude: float | Unset = UNSET,
    orderby: QueryFdsnEventsOrderby | Unset = QueryFdsnEventsOrderby.TIME,
    limit: int | Unset = UNSET,
    format_: QueryFdsnEventsFormat | Unset = QueryFdsnEventsFormat.XML,
    nodata: QueryFdsnEventsNodata | Unset = QueryFdsnEventsNodata.VALUE_204,
) -> Response[Any | Problem | str]:
    """FDSN event query

     fdsnws-event 1.2 over `earthquake` records: QuakeML 1.2 BED (`smi:foresignal/{source}/{externalId}`
    publicIDs) or the pipe-separated text format. `204` when no events match (FDSN convention;
    `nodata=404` selects 404); errors use FDSN plain-text bodies. Long and short parameter names are
    accepted (`start`, `minlat`, `minmag`, …); unsupported parameters are a 400. Depth is in meters in
    QuakeML and in km in the text format.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        starttime (datetime.datetime | Unset):
        endtime (datetime.datetime | Unset):
        minlatitude (float | Unset):
        maxlatitude (float | Unset):
        minlongitude (float | Unset):
        maxlongitude (float | Unset):
        latitude (float | Unset):
        longitude (float | Unset):
        minradius (float | Unset):
        maxradius (float | Unset):
        minmagnitude (float | Unset):
        maxmagnitude (float | Unset):
        orderby (QueryFdsnEventsOrderby | Unset):  Default: QueryFdsnEventsOrderby.TIME.
        limit (int | Unset):
        format_ (QueryFdsnEventsFormat | Unset):  Default: QueryFdsnEventsFormat.XML.
        nodata (QueryFdsnEventsNodata | Unset):  Default: QueryFdsnEventsNodata.VALUE_204.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        starttime=starttime,
        endtime=endtime,
        minlatitude=minlatitude,
        maxlatitude=maxlatitude,
        minlongitude=minlongitude,
        maxlongitude=maxlongitude,
        latitude=latitude,
        longitude=longitude,
        minradius=minradius,
        maxradius=maxradius,
        minmagnitude=minmagnitude,
        maxmagnitude=maxmagnitude,
        orderby=orderby,
        limit=limit,
        format_=format_,
        nodata=nodata,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    starttime: datetime.datetime | Unset = UNSET,
    endtime: datetime.datetime | Unset = UNSET,
    minlatitude: float | Unset = UNSET,
    maxlatitude: float | Unset = UNSET,
    minlongitude: float | Unset = UNSET,
    maxlongitude: float | Unset = UNSET,
    latitude: float | Unset = UNSET,
    longitude: float | Unset = UNSET,
    minradius: float | Unset = UNSET,
    maxradius: float | Unset = UNSET,
    minmagnitude: float | Unset = UNSET,
    maxmagnitude: float | Unset = UNSET,
    orderby: QueryFdsnEventsOrderby | Unset = QueryFdsnEventsOrderby.TIME,
    limit: int | Unset = UNSET,
    format_: QueryFdsnEventsFormat | Unset = QueryFdsnEventsFormat.XML,
    nodata: QueryFdsnEventsNodata | Unset = QueryFdsnEventsNodata.VALUE_204,
) -> Any | Problem | str | None:
    """FDSN event query

     fdsnws-event 1.2 over `earthquake` records: QuakeML 1.2 BED (`smi:foresignal/{source}/{externalId}`
    publicIDs) or the pipe-separated text format. `204` when no events match (FDSN convention;
    `nodata=404` selects 404); errors use FDSN plain-text bodies. Long and short parameter names are
    accepted (`start`, `minlat`, `minmag`, …); unsupported parameters are a 400. Depth is in meters in
    QuakeML and in km in the text format.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        starttime (datetime.datetime | Unset):
        endtime (datetime.datetime | Unset):
        minlatitude (float | Unset):
        maxlatitude (float | Unset):
        minlongitude (float | Unset):
        maxlongitude (float | Unset):
        latitude (float | Unset):
        longitude (float | Unset):
        minradius (float | Unset):
        maxradius (float | Unset):
        minmagnitude (float | Unset):
        maxmagnitude (float | Unset):
        orderby (QueryFdsnEventsOrderby | Unset):  Default: QueryFdsnEventsOrderby.TIME.
        limit (int | Unset):
        format_ (QueryFdsnEventsFormat | Unset):  Default: QueryFdsnEventsFormat.XML.
        nodata (QueryFdsnEventsNodata | Unset):  Default: QueryFdsnEventsNodata.VALUE_204.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return (
        await asyncio_detailed(
            client=client,
            starttime=starttime,
            endtime=endtime,
            minlatitude=minlatitude,
            maxlatitude=maxlatitude,
            minlongitude=minlongitude,
            maxlongitude=maxlongitude,
            latitude=latitude,
            longitude=longitude,
            minradius=minradius,
            maxradius=maxradius,
            minmagnitude=minmagnitude,
            maxmagnitude=maxmagnitude,
            orderby=orderby,
            limit=limit,
            format_=format_,
            nodata=nodata,
        )
    ).parsed
