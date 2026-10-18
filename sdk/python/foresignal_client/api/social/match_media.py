from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.media_match_request import MediaMatchRequest
from ...models.media_match_result import MediaMatchResult
from ...models.media_match_upload import MediaMatchUpload
from ...models.problem import Problem
from ...types import UNSET, Response


def _get_kwargs(
    *,
    body: MediaMatchUpload | MediaMatchRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/social/media/match",
    }

    if isinstance(body, MediaMatchUpload):
        _kwargs["files"] = body.to_multipart()

        headers["Content-Type"] = "multipart/form-data; boundary=+++"
    if isinstance(body, MediaMatchRequest):
        _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MediaMatchResult | Problem | None:
    if response.status_code == 200:
        response_200 = MediaMatchResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[MediaMatchResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: MediaMatchUpload | MediaMatchRequest | Unset = UNSET,
) -> Response[MediaMatchResult | Problem]:
    """Match an image against fingerprints

     Upload an image (multipart, through 043 ImageGuard) or submit a PDQ hex string; returns fingerprints
    within Hamming distance 31 (default) with first-seen dates and narratives, and the REFERENCE_WINDOW
    caveat stating the fingerprint store's start date. The uploaded image is not stored.

    **Scope:** `read` (token required).

    Args:
        body (MediaMatchUpload): Multipart image upload for matching (through 043 ImageGuard:
            size, type and SSRF limits)
        body (MediaMatchRequest): Match a PDQ hash against the fingerprint store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaMatchResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: MediaMatchUpload | MediaMatchRequest | Unset = UNSET,
) -> MediaMatchResult | Problem | None:
    """Match an image against fingerprints

     Upload an image (multipart, through 043 ImageGuard) or submit a PDQ hex string; returns fingerprints
    within Hamming distance 31 (default) with first-seen dates and narratives, and the REFERENCE_WINDOW
    caveat stating the fingerprint store's start date. The uploaded image is not stored.

    **Scope:** `read` (token required).

    Args:
        body (MediaMatchUpload): Multipart image upload for matching (through 043 ImageGuard:
            size, type and SSRF limits)
        body (MediaMatchRequest): Match a PDQ hash against the fingerprint store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaMatchResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: MediaMatchUpload | MediaMatchRequest | Unset = UNSET,
) -> Response[MediaMatchResult | Problem]:
    """Match an image against fingerprints

     Upload an image (multipart, through 043 ImageGuard) or submit a PDQ hex string; returns fingerprints
    within Hamming distance 31 (default) with first-seen dates and narratives, and the REFERENCE_WINDOW
    caveat stating the fingerprint store's start date. The uploaded image is not stored.

    **Scope:** `read` (token required).

    Args:
        body (MediaMatchUpload): Multipart image upload for matching (through 043 ImageGuard:
            size, type and SSRF limits)
        body (MediaMatchRequest): Match a PDQ hash against the fingerprint store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaMatchResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: MediaMatchUpload | MediaMatchRequest | Unset = UNSET,
) -> MediaMatchResult | Problem | None:
    """Match an image against fingerprints

     Upload an image (multipart, through 043 ImageGuard) or submit a PDQ hex string; returns fingerprints
    within Hamming distance 31 (default) with first-seen dates and narratives, and the REFERENCE_WINDOW
    caveat stating the fingerprint store's start date. The uploaded image is not stored.

    **Scope:** `read` (token required).

    Args:
        body (MediaMatchUpload): Multipart image upload for matching (through 043 ImageGuard:
            size, type and SSRF limits)
        body (MediaMatchRequest): Match a PDQ hash against the fingerprint store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaMatchResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
