from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.exercise_run import ExerciseRun
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    exercise_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/exercises/{exercise_id}".format(
            exercise_id=quote(str(exercise_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExerciseRun | Problem | None:
    if response.status_code == 200:
        response_200 = ExerciseRun.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExerciseRun | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    exercise_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExerciseRun | Problem]:
    """Get an exercise run

     Status and statistics of a run.

    **Scope:** `agent`.

    Args:
        exercise_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExerciseRun | Problem]
    """

    kwargs = _get_kwargs(
        exercise_id=exercise_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    exercise_id: str,
    *,
    client: AuthenticatedClient,
) -> ExerciseRun | Problem | None:
    """Get an exercise run

     Status and statistics of a run.

    **Scope:** `agent`.

    Args:
        exercise_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExerciseRun | Problem
    """

    return sync_detailed(
        exercise_id=exercise_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    exercise_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExerciseRun | Problem]:
    """Get an exercise run

     Status and statistics of a run.

    **Scope:** `agent`.

    Args:
        exercise_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExerciseRun | Problem]
    """

    kwargs = _get_kwargs(
        exercise_id=exercise_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    exercise_id: str,
    *,
    client: AuthenticatedClient,
) -> ExerciseRun | Problem | None:
    """Get an exercise run

     Status and statistics of a run.

    **Scope:** `agent`.

    Args:
        exercise_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExerciseRun | Problem
    """

    return (
        await asyncio_detailed(
            exercise_id=exercise_id,
            client=client,
        )
    ).parsed
