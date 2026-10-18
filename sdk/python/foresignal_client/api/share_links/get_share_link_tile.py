from http import HTTPStatus
from io import BytesIO
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import File, Response


def _get_kwargs(
    key: str,
    z: int,
    x: int,
    y: int,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/k/{key}/tiles/{z}/{x}/{y}.mvt".format(
            key=quote(str(key), safe=""),
            z=quote(str(z), safe=""),
            x=quote(str(x), safe=""),
            y=quote(str(y), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
) -> Response[File | Problem]:
    """Vector tile of a shared view or track

     MVT tiles for shared views, within the creator's restriction and the view, without private or non-
    redistributable sources. 404 for other target types (a track is served as GeoJSON) and for links
    with a privacy mask, a precision cap or a field allowlist, which tiles cannot carry.

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        z (int):
        x (int):
        y (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        key=key,
        z=z,
        x=x,
        y=y,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
) -> File | Problem | None:
    """Vector tile of a shared view or track

     MVT tiles for shared views, within the creator's restriction and the view, without private or non-
    redistributable sources. 404 for other target types (a track is served as GeoJSON) and for links
    with a privacy mask, a precision cap or a field allowlist, which tiles cannot carry.

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        z (int):
        x (int):
        y (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return sync_detailed(
        key=key,
        z=z,
        x=x,
        y=y,
        client=client,
    ).parsed


async def asyncio_detailed(
    key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
) -> Response[File | Problem]:
    """Vector tile of a shared view or track

     MVT tiles for shared views, within the creator's restriction and the view, without private or non-
    redistributable sources. 404 for other target types (a track is served as GeoJSON) and for links
    with a privacy mask, a precision cap or a field allowlist, which tiles cannot carry.

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        z (int):
        x (int):
        y (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        key=key,
        z=z,
        x=x,
        y=y,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
) -> File | Problem | None:
    """Vector tile of a shared view or track

     MVT tiles for shared views, within the creator's restriction and the view, without private or non-
    redistributable sources. 404 for other target types (a track is served as GeoJSON) and for links
    with a privacy mask, a precision cap or a field allowlist, which tiles cannot carry.

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        z (int):
        x (int):
        y (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return (
        await asyncio_detailed(
            key=key,
            z=z,
            x=x,
            y=y,
            client=client,
        )
    ).parsed
