from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.open_share_link_open import OpenShareLinkOpen
from ...models.problem import Problem
from ...models.shared_content import SharedContent
from ...types import UNSET, Response, Unset


def _get_kwargs(
    key: str,
    *,
    open_: OpenShareLinkOpen | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_open_: int | Unset = UNSET
    if not isinstance(open_, Unset):
        json_open_ = open_.value

    params["open"] = json_open_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/k/{key}".format(
            key=quote(str(key), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | SharedContent | None:
    if response.status_code == 200:
        response_200 = SharedContent.from_dict(response.json())

        return response_200

    if response.status_code == 302:
        response_302 = cast(Any, None)
        return response_302

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Any | Problem | SharedContent]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    key: str,
    *,
    client: AuthenticatedClient,
    open_: OpenShareLinkOpen | Unset = UNSET,
) -> Response[Any | Problem | SharedContent]:
    """Open a share link

     Resolves the key server-side to its target and returns it masked, licence-filtered and with
    attribution: JSON, or GeoJSON with `Accept: application/geo+json`. No HTML. With `open=1` and
    `clientLinks.share` configured, redirects (302) to the client template with the key. Uses are
    counted and audited (sampled).

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        open_ (OpenShareLinkOpen | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | SharedContent]
    """

    kwargs = _get_kwargs(
        key=key,
        open_=open_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    key: str,
    *,
    client: AuthenticatedClient,
    open_: OpenShareLinkOpen | Unset = UNSET,
) -> Any | Problem | SharedContent | None:
    """Open a share link

     Resolves the key server-side to its target and returns it masked, licence-filtered and with
    attribution: JSON, or GeoJSON with `Accept: application/geo+json`. No HTML. With `open=1` and
    `clientLinks.share` configured, redirects (302) to the client template with the key. Uses are
    counted and audited (sampled).

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        open_ (OpenShareLinkOpen | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | SharedContent
    """

    return sync_detailed(
        key=key,
        client=client,
        open_=open_,
    ).parsed


async def asyncio_detailed(
    key: str,
    *,
    client: AuthenticatedClient,
    open_: OpenShareLinkOpen | Unset = UNSET,
) -> Response[Any | Problem | SharedContent]:
    """Open a share link

     Resolves the key server-side to its target and returns it masked, licence-filtered and with
    attribution: JSON, or GeoJSON with `Accept: application/geo+json`. No HTML. With `open=1` and
    `clientLinks.share` configured, redirects (302) to the client template with the key. Uses are
    counted and audited (sampled).

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        open_ (OpenShareLinkOpen | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | SharedContent]
    """

    kwargs = _get_kwargs(
        key=key,
        open_=open_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    key: str,
    *,
    client: AuthenticatedClient,
    open_: OpenShareLinkOpen | Unset = UNSET,
) -> Any | Problem | SharedContent | None:
    """Open a share link

     Resolves the key server-side to its target and returns it masked, licence-filtered and with
    attribution: JSON, or GeoJSON with `Accept: application/geo+json`. No HTML. With `open=1` and
    `clientLinks.share` configured, redirects (302) to the client template with the key. Uses are
    counted and audited (sampled).

    **Scope:** `open` (the `fss_` share key in the path is the credential; it is never accepted as a
    bearer token).

    Args:
        key (str):
        open_ (OpenShareLinkOpen | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | SharedContent
    """

    return (
        await asyncio_detailed(
            key=key,
            client=client,
            open_=open_,
        )
    ).parsed
