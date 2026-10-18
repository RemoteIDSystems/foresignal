from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...types import UNSET, Response, Unset


def _get_kwargs(
    group: str,
    *,
    key: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["key"] = key

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/compat/celestrak/NORAD/elements/{group}.txt".format(
            group=quote(str(group), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = cast(Any, None)
        return response_400

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    group: str,
    *,
    client: AuthenticatedClient,
    key: str | Unset = UNSET,
) -> Response[Any | str]:
    """Per-group TLE text (CelesTrak-compatible)

     The two-line element sets of a CelesTrak group as plain TLE text, for SatDump, Gpredict and
    Look4Sat. Served from the celestrak-gp cache only; never fetched upstream.

    Args:
        group (str):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | str]
    """

    kwargs = _get_kwargs(
        group=group,
        key=key,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    group: str,
    *,
    client: AuthenticatedClient,
    key: str | Unset = UNSET,
) -> Any | str | None:
    """Per-group TLE text (CelesTrak-compatible)

     The two-line element sets of a CelesTrak group as plain TLE text, for SatDump, Gpredict and
    Look4Sat. Served from the celestrak-gp cache only; never fetched upstream.

    Args:
        group (str):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | str
    """

    return sync_detailed(
        group=group,
        client=client,
        key=key,
    ).parsed


async def asyncio_detailed(
    group: str,
    *,
    client: AuthenticatedClient,
    key: str | Unset = UNSET,
) -> Response[Any | str]:
    """Per-group TLE text (CelesTrak-compatible)

     The two-line element sets of a CelesTrak group as plain TLE text, for SatDump, Gpredict and
    Look4Sat. Served from the celestrak-gp cache only; never fetched upstream.

    Args:
        group (str):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | str]
    """

    kwargs = _get_kwargs(
        group=group,
        key=key,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    group: str,
    *,
    client: AuthenticatedClient,
    key: str | Unset = UNSET,
) -> Any | str | None:
    """Per-group TLE text (CelesTrak-compatible)

     The two-line element sets of a CelesTrak group as plain TLE text, for SatDump, Gpredict and
    Look4Sat. Served from the celestrak-gp cache only; never fetched upstream.

    Args:
        group (str):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | str
    """

    return (
        await asyncio_detailed(
            group=group,
            client=client,
            key=key,
        )
    ).parsed
