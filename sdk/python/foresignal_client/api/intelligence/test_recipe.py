from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.recipe_test_request import RecipeTestRequest
from ...models.recipe_test_result import RecipeTestResult
from ...types import Response


def _get_kwargs(
    recipe_id: str,
    *,
    body: RecipeTestRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/intel/recipes/{recipe_id}/test".format(
            recipe_id=quote(str(recipe_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RecipeTestResult | None:
    if response.status_code == 200:
        response_200 = RecipeTestResult.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RecipeTestResult]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    recipe_id: str,
    *,
    client: AuthenticatedClient,
    body: RecipeTestRequest,
) -> Response[Problem | RecipeTestResult]:
    """Dry-run a recipe over a seq range

     Runs the recipe into an in-memory store and returns would-be events; nothing is written.

    **Scope:** `agent` or `admin`.

    Args:
        recipe_id (str):
        body (RecipeTestRequest): Dry-run range

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RecipeTestResult]
    """

    kwargs = _get_kwargs(
        recipe_id=recipe_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    recipe_id: str,
    *,
    client: AuthenticatedClient,
    body: RecipeTestRequest,
) -> Problem | RecipeTestResult | None:
    """Dry-run a recipe over a seq range

     Runs the recipe into an in-memory store and returns would-be events; nothing is written.

    **Scope:** `agent` or `admin`.

    Args:
        recipe_id (str):
        body (RecipeTestRequest): Dry-run range

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RecipeTestResult
    """

    return sync_detailed(
        recipe_id=recipe_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    recipe_id: str,
    *,
    client: AuthenticatedClient,
    body: RecipeTestRequest,
) -> Response[Problem | RecipeTestResult]:
    """Dry-run a recipe over a seq range

     Runs the recipe into an in-memory store and returns would-be events; nothing is written.

    **Scope:** `agent` or `admin`.

    Args:
        recipe_id (str):
        body (RecipeTestRequest): Dry-run range

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RecipeTestResult]
    """

    kwargs = _get_kwargs(
        recipe_id=recipe_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    recipe_id: str,
    *,
    client: AuthenticatedClient,
    body: RecipeTestRequest,
) -> Problem | RecipeTestResult | None:
    """Dry-run a recipe over a seq range

     Runs the recipe into an in-memory store and returns would-be events; nothing is written.

    **Scope:** `agent` or `admin`.

    Args:
        recipe_id (str):
        body (RecipeTestRequest): Dry-run range

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RecipeTestResult
    """

    return (
        await asyncio_detailed(
            recipe_id=recipe_id,
            client=client,
            body=body,
        )
    ).parsed
