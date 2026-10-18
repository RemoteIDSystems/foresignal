from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.precursor_test import PrecursorTest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    precursor_test_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/precursor-tests/{precursor_test_id}".format(
            precursor_test_id=quote(str(precursor_test_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PrecursorTest | Problem | None:
    if response.status_code == 200:
        response_200 = PrecursorTest.from_dict(response.json())

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
) -> Response[PrecursorTest | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PrecursorTest | Problem]:
    """Get a precursor test

     One precursor test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PrecursorTest | Problem]
    """

    kwargs = _get_kwargs(
        precursor_test_id=precursor_test_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
) -> PrecursorTest | Problem | None:
    """Get a precursor test

     One precursor test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PrecursorTest | Problem
    """

    return sync_detailed(
        precursor_test_id=precursor_test_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PrecursorTest | Problem]:
    """Get a precursor test

     One precursor test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PrecursorTest | Problem]
    """

    kwargs = _get_kwargs(
        precursor_test_id=precursor_test_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
) -> PrecursorTest | Problem | None:
    """Get a precursor test

     One precursor test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PrecursorTest | Problem
    """

    return (
        await asyncio_detailed(
            precursor_test_id=precursor_test_id,
            client=client,
        )
    ).parsed
