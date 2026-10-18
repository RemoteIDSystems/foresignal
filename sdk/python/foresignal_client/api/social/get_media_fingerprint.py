from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.media_fingerprint import MediaFingerprint
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    fingerprint_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/media/{fingerprint_id}".format(
            fingerprint_id=quote(str(fingerprint_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MediaFingerprint | Problem | None:
    if response.status_code == 200:
        response_200 = MediaFingerprint.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

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
) -> Response[MediaFingerprint | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    fingerprint_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[MediaFingerprint | Problem]:
    """Get a media fingerprint

     A PDQ fingerprint with first-seen date and post, uses, platforms, narratives and reuse findings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        fingerprint_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaFingerprint | Problem]
    """

    kwargs = _get_kwargs(
        fingerprint_id=fingerprint_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    fingerprint_id: str,
    *,
    client: AuthenticatedClient,
) -> MediaFingerprint | Problem | None:
    """Get a media fingerprint

     A PDQ fingerprint with first-seen date and post, uses, platforms, narratives and reuse findings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        fingerprint_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaFingerprint | Problem
    """

    return sync_detailed(
        fingerprint_id=fingerprint_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    fingerprint_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[MediaFingerprint | Problem]:
    """Get a media fingerprint

     A PDQ fingerprint with first-seen date and post, uses, platforms, narratives and reuse findings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        fingerprint_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaFingerprint | Problem]
    """

    kwargs = _get_kwargs(
        fingerprint_id=fingerprint_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    fingerprint_id: str,
    *,
    client: AuthenticatedClient,
) -> MediaFingerprint | Problem | None:
    """Get a media fingerprint

     A PDQ fingerprint with first-seen date and post, uses, platforms, narratives and reuse findings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        fingerprint_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaFingerprint | Problem
    """

    return (
        await asyncio_detailed(
            fingerprint_id=fingerprint_id,
            client=client,
        )
    ).parsed
