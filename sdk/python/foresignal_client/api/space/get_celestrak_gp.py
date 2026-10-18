from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    group: str | Unset = UNSET,
    catnr: str | Unset = UNSET,
    intdes: str | Unset = UNSET,
    name: str | Unset = UNSET,
    special: str | Unset = UNSET,
    format_: str | Unset = UNSET,
    key: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["GROUP"] = group

    params["CATNR"] = catnr

    params["INTDES"] = intdes

    params["NAME"] = name

    params["SPECIAL"] = special

    params["FORMAT"] = format_

    params["key"] = key

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/compat/celestrak/gp.php",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = response.text
        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

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
    group: str | Unset = UNSET,
    catnr: str | Unset = UNSET,
    intdes: str | Unset = UNSET,
    name: str | Unset = UNSET,
    special: str | Unset = UNSET,
    format_: str | Unset = UNSET,
    key: str | Unset = UNSET,
) -> Response[Any | Problem | str]:
    """CelesTrak-compatible GP element sets

     CelesTrak-compatible GP output served from `celestrak-gp` heads, never by calling CelesTrak, so
    Gpredict (`GROUP=…&FORMAT=tle`), Look4Sat (`GROUP=active&FORMAT=csv`) and SatDump (`FORMAT=json`)
    can read elements from Foresignal. Query keys are uppercase as at CelesTrak; exactly one selector is
    required (`INTDES` matches as a prefix, so `1998-067` selects every piece). Clients that cannot send
    headers pass a 035 feed key as `?key=` (the key's prefixes must include `/compat/celestrak/`); a
    feed-key response is `Cache-Control: private, max-age=3600`. A caller whose restriction hides
    `celestrak-gp` or the `satellite` kind, or limits the area, gets `No GP data found`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        group (str | Unset):  Example: stations.
        catnr (str | Unset):
        intdes (str | Unset):
        name (str | Unset):
        special (str | Unset):
        format_ (str | Unset):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        group=group,
        catnr=catnr,
        intdes=intdes,
        name=name,
        special=special,
        format_=format_,
        key=key,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    group: str | Unset = UNSET,
    catnr: str | Unset = UNSET,
    intdes: str | Unset = UNSET,
    name: str | Unset = UNSET,
    special: str | Unset = UNSET,
    format_: str | Unset = UNSET,
    key: str | Unset = UNSET,
) -> Any | Problem | str | None:
    """CelesTrak-compatible GP element sets

     CelesTrak-compatible GP output served from `celestrak-gp` heads, never by calling CelesTrak, so
    Gpredict (`GROUP=…&FORMAT=tle`), Look4Sat (`GROUP=active&FORMAT=csv`) and SatDump (`FORMAT=json`)
    can read elements from Foresignal. Query keys are uppercase as at CelesTrak; exactly one selector is
    required (`INTDES` matches as a prefix, so `1998-067` selects every piece). Clients that cannot send
    headers pass a 035 feed key as `?key=` (the key's prefixes must include `/compat/celestrak/`); a
    feed-key response is `Cache-Control: private, max-age=3600`. A caller whose restriction hides
    `celestrak-gp` or the `satellite` kind, or limits the area, gets `No GP data found`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        group (str | Unset):  Example: stations.
        catnr (str | Unset):
        intdes (str | Unset):
        name (str | Unset):
        special (str | Unset):
        format_ (str | Unset):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return sync_detailed(
        client=client,
        group=group,
        catnr=catnr,
        intdes=intdes,
        name=name,
        special=special,
        format_=format_,
        key=key,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    group: str | Unset = UNSET,
    catnr: str | Unset = UNSET,
    intdes: str | Unset = UNSET,
    name: str | Unset = UNSET,
    special: str | Unset = UNSET,
    format_: str | Unset = UNSET,
    key: str | Unset = UNSET,
) -> Response[Any | Problem | str]:
    """CelesTrak-compatible GP element sets

     CelesTrak-compatible GP output served from `celestrak-gp` heads, never by calling CelesTrak, so
    Gpredict (`GROUP=…&FORMAT=tle`), Look4Sat (`GROUP=active&FORMAT=csv`) and SatDump (`FORMAT=json`)
    can read elements from Foresignal. Query keys are uppercase as at CelesTrak; exactly one selector is
    required (`INTDES` matches as a prefix, so `1998-067` selects every piece). Clients that cannot send
    headers pass a 035 feed key as `?key=` (the key's prefixes must include `/compat/celestrak/`); a
    feed-key response is `Cache-Control: private, max-age=3600`. A caller whose restriction hides
    `celestrak-gp` or the `satellite` kind, or limits the area, gets `No GP data found`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        group (str | Unset):  Example: stations.
        catnr (str | Unset):
        intdes (str | Unset):
        name (str | Unset):
        special (str | Unset):
        format_ (str | Unset):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        group=group,
        catnr=catnr,
        intdes=intdes,
        name=name,
        special=special,
        format_=format_,
        key=key,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    group: str | Unset = UNSET,
    catnr: str | Unset = UNSET,
    intdes: str | Unset = UNSET,
    name: str | Unset = UNSET,
    special: str | Unset = UNSET,
    format_: str | Unset = UNSET,
    key: str | Unset = UNSET,
) -> Any | Problem | str | None:
    """CelesTrak-compatible GP element sets

     CelesTrak-compatible GP output served from `celestrak-gp` heads, never by calling CelesTrak, so
    Gpredict (`GROUP=…&FORMAT=tle`), Look4Sat (`GROUP=active&FORMAT=csv`) and SatDump (`FORMAT=json`)
    can read elements from Foresignal. Query keys are uppercase as at CelesTrak; exactly one selector is
    required (`INTDES` matches as a prefix, so `1998-067` selects every piece). Clients that cannot send
    headers pass a 035 feed key as `?key=` (the key's prefixes must include `/compat/celestrak/`); a
    feed-key response is `Cache-Control: private, max-age=3600`. A caller whose restriction hides
    `celestrak-gp` or the `satellite` kind, or limits the area, gets `No GP data found`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        group (str | Unset):  Example: stations.
        catnr (str | Unset):
        intdes (str | Unset):
        name (str | Unset):
        special (str | Unset):
        format_ (str | Unset):
        key (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return (
        await asyncio_detailed(
            client=client,
            group=group,
            catnr=catnr,
            intdes=intdes,
            name=name,
            special=special,
            format_=format_,
            key=key,
        )
    ).parsed
