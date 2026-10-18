from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ach_matrix import AchMatrix
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    set_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/hypotheses/sets/{set_id}/matrix".format(
            set_id=quote(str(set_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AchMatrix | Problem | None:
    if response.status_code == 200:
        response_200 = AchMatrix.from_dict(response.json())

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
) -> Response[AchMatrix | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    set_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[AchMatrix | Problem]:
    """Get the ACH matrix

     Hypotheses, evidence rows with ratings and weights, weighted inconsistency scores, the least-
    inconsistent hypothesis and non-diagnostic flags. An audit view: probabilities come from the
    updater.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AchMatrix | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    set_id: str,
    *,
    client: AuthenticatedClient,
) -> AchMatrix | Problem | None:
    """Get the ACH matrix

     Hypotheses, evidence rows with ratings and weights, weighted inconsistency scores, the least-
    inconsistent hypothesis and non-diagnostic flags. An audit view: probabilities come from the
    updater.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AchMatrix | Problem
    """

    return sync_detailed(
        set_id=set_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    set_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[AchMatrix | Problem]:
    """Get the ACH matrix

     Hypotheses, evidence rows with ratings and weights, weighted inconsistency scores, the least-
    inconsistent hypothesis and non-diagnostic flags. An audit view: probabilities come from the
    updater.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AchMatrix | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    set_id: str,
    *,
    client: AuthenticatedClient,
) -> AchMatrix | Problem | None:
    """Get the ACH matrix

     Hypotheses, evidence rows with ratings and weights, weighted inconsistency scores, the least-
    inconsistent hypothesis and non-diagnostic flags. An audit view: probabilities come from the
    updater.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AchMatrix | Problem
    """

    return (
        await asyncio_detailed(
            set_id=set_id,
            client=client,
        )
    ).parsed
