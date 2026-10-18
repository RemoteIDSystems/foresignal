from http import HTTPStatus
from io import BytesIO
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_wmts_kvp_format import GetWmtsKvpFORMAT
from ...models.get_wmts_kvp_request import GetWmtsKvpREQUEST
from ...models.get_wmts_kvp_service import GetWmtsKvpSERVICE
from ...models.get_wmts_kvp_tilematrixset import GetWmtsKvpTILEMATRIXSET
from ...models.get_wmts_kvp_version import GetWmtsKvpVERSION
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    *,
    service: GetWmtsKvpSERVICE,
    request: GetWmtsKvpREQUEST,
    version: GetWmtsKvpVERSION | Unset = UNSET,
    layer: str | Unset = UNSET,
    style: str | Unset = UNSET,
    format_: GetWmtsKvpFORMAT | Unset = UNSET,
    tilematrixset: GetWmtsKvpTILEMATRIXSET | Unset = UNSET,
    tilematrix: str | Unset = UNSET,
    tilerow: int | Unset = UNSET,
    tilecol: int | Unset = UNSET,
    time: str | Unset = UNSET,
    reftime: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_service = service.value
    params["SERVICE"] = json_service

    json_request = request.value
    params["REQUEST"] = json_request

    json_version: str | Unset = UNSET
    if not isinstance(version, Unset):
        json_version = version.value

    params["VERSION"] = json_version

    params["LAYER"] = layer

    params["STYLE"] = style

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["FORMAT"] = json_format_

    json_tilematrixset: str | Unset = UNSET
    if not isinstance(tilematrixset, Unset):
        json_tilematrixset = tilematrixset.value

    params["TILEMATRIXSET"] = json_tilematrixset

    params["TILEMATRIX"] = tilematrix

    params["TILEROW"] = tilerow

    params["TILECOL"] = tilecol

    params["TIME"] = time

    params["REFTIME"] = reftime

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/wmts",
        "params": params,
    }

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

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 406:
        response_406 = Problem.from_dict(response.json())

        return response_406

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

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
    *,
    client: AuthenticatedClient,
    service: GetWmtsKvpSERVICE,
    request: GetWmtsKvpREQUEST,
    version: GetWmtsKvpVERSION | Unset = UNSET,
    layer: str | Unset = UNSET,
    style: str | Unset = UNSET,
    format_: GetWmtsKvpFORMAT | Unset = UNSET,
    tilematrixset: GetWmtsKvpTILEMATRIXSET | Unset = UNSET,
    tilematrix: str | Unset = UNSET,
    tilerow: int | Unset = UNSET,
    tilecol: int | Unset = UNSET,
    time: str | Unset = UNSET,
    reftime: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """WMTS key-value-pair requests

     `SERVICE=WMTS&REQUEST=GetCapabilities|GetTile`. GetTile with a concrete TIME 301-redirects to the
    content-addressed tile; `current` maps to the `latest` alias.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        service (GetWmtsKvpSERVICE):
        request (GetWmtsKvpREQUEST):
        version (GetWmtsKvpVERSION | Unset):
        layer (str | Unset):
        style (str | Unset):
        format_ (GetWmtsKvpFORMAT | Unset):
        tilematrixset (GetWmtsKvpTILEMATRIXSET | Unset):
        tilematrix (str | Unset):
        tilerow (int | Unset):
        tilecol (int | Unset):
        time (str | Unset):
        reftime (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        service=service,
        request=request,
        version=version,
        layer=layer,
        style=style,
        format_=format_,
        tilematrixset=tilematrixset,
        tilematrix=tilematrix,
        tilerow=tilerow,
        tilecol=tilecol,
        time=time,
        reftime=reftime,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    service: GetWmtsKvpSERVICE,
    request: GetWmtsKvpREQUEST,
    version: GetWmtsKvpVERSION | Unset = UNSET,
    layer: str | Unset = UNSET,
    style: str | Unset = UNSET,
    format_: GetWmtsKvpFORMAT | Unset = UNSET,
    tilematrixset: GetWmtsKvpTILEMATRIXSET | Unset = UNSET,
    tilematrix: str | Unset = UNSET,
    tilerow: int | Unset = UNSET,
    tilecol: int | Unset = UNSET,
    time: str | Unset = UNSET,
    reftime: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """WMTS key-value-pair requests

     `SERVICE=WMTS&REQUEST=GetCapabilities|GetTile`. GetTile with a concrete TIME 301-redirects to the
    content-addressed tile; `current` maps to the `latest` alias.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        service (GetWmtsKvpSERVICE):
        request (GetWmtsKvpREQUEST):
        version (GetWmtsKvpVERSION | Unset):
        layer (str | Unset):
        style (str | Unset):
        format_ (GetWmtsKvpFORMAT | Unset):
        tilematrixset (GetWmtsKvpTILEMATRIXSET | Unset):
        tilematrix (str | Unset):
        tilerow (int | Unset):
        tilecol (int | Unset):
        time (str | Unset):
        reftime (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        client=client,
        service=service,
        request=request,
        version=version,
        layer=layer,
        style=style,
        format_=format_,
        tilematrixset=tilematrixset,
        tilematrix=tilematrix,
        tilerow=tilerow,
        tilecol=tilecol,
        time=time,
        reftime=reftime,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    service: GetWmtsKvpSERVICE,
    request: GetWmtsKvpREQUEST,
    version: GetWmtsKvpVERSION | Unset = UNSET,
    layer: str | Unset = UNSET,
    style: str | Unset = UNSET,
    format_: GetWmtsKvpFORMAT | Unset = UNSET,
    tilematrixset: GetWmtsKvpTILEMATRIXSET | Unset = UNSET,
    tilematrix: str | Unset = UNSET,
    tilerow: int | Unset = UNSET,
    tilecol: int | Unset = UNSET,
    time: str | Unset = UNSET,
    reftime: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """WMTS key-value-pair requests

     `SERVICE=WMTS&REQUEST=GetCapabilities|GetTile`. GetTile with a concrete TIME 301-redirects to the
    content-addressed tile; `current` maps to the `latest` alias.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        service (GetWmtsKvpSERVICE):
        request (GetWmtsKvpREQUEST):
        version (GetWmtsKvpVERSION | Unset):
        layer (str | Unset):
        style (str | Unset):
        format_ (GetWmtsKvpFORMAT | Unset):
        tilematrixset (GetWmtsKvpTILEMATRIXSET | Unset):
        tilematrix (str | Unset):
        tilerow (int | Unset):
        tilecol (int | Unset):
        time (str | Unset):
        reftime (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        service=service,
        request=request,
        version=version,
        layer=layer,
        style=style,
        format_=format_,
        tilematrixset=tilematrixset,
        tilematrix=tilematrix,
        tilerow=tilerow,
        tilecol=tilecol,
        time=time,
        reftime=reftime,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    service: GetWmtsKvpSERVICE,
    request: GetWmtsKvpREQUEST,
    version: GetWmtsKvpVERSION | Unset = UNSET,
    layer: str | Unset = UNSET,
    style: str | Unset = UNSET,
    format_: GetWmtsKvpFORMAT | Unset = UNSET,
    tilematrixset: GetWmtsKvpTILEMATRIXSET | Unset = UNSET,
    tilematrix: str | Unset = UNSET,
    tilerow: int | Unset = UNSET,
    tilecol: int | Unset = UNSET,
    time: str | Unset = UNSET,
    reftime: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """WMTS key-value-pair requests

     `SERVICE=WMTS&REQUEST=GetCapabilities|GetTile`. GetTile with a concrete TIME 301-redirects to the
    content-addressed tile; `current` maps to the `latest` alias.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        service (GetWmtsKvpSERVICE):
        request (GetWmtsKvpREQUEST):
        version (GetWmtsKvpVERSION | Unset):
        layer (str | Unset):
        style (str | Unset):
        format_ (GetWmtsKvpFORMAT | Unset):
        tilematrixset (GetWmtsKvpTILEMATRIXSET | Unset):
        tilematrix (str | Unset):
        tilerow (int | Unset):
        tilecol (int | Unset):
        time (str | Unset):
        reftime (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            service=service,
            request=request,
            version=version,
            layer=layer,
            style=style,
            format_=format_,
            tilematrixset=tilematrixset,
            tilematrix=tilematrix,
            tilerow=tilerow,
            tilecol=tilecol,
            time=time,
            reftime=reftime,
        )
    ).parsed
