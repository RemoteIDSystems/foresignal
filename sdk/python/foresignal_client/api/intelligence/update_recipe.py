from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.correlation_recipe import CorrelationRecipe
from ...models.correlation_recipe_update import CorrelationRecipeUpdate
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    recipe_id: str,
    *,
    body: CorrelationRecipeUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/intel/recipes/{recipe_id}".format(
            recipe_id=quote(str(recipe_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CorrelationRecipe | Problem | None:
    if response.status_code == 200:
        response_200 = CorrelationRecipe.from_dict(response.json())

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
) -> Response[CorrelationRecipe | Problem]:
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
    body: CorrelationRecipeUpdate,
) -> Response[CorrelationRecipe | Problem]:
    """Enable or tune a recipe

     Sets `enabled` and parameter overrides; overrides survive built-in version updates.

    **Scope:** `admin`.

    Args:
        recipe_id (str):
        body (CorrelationRecipeUpdate): Operator changes to a recipe

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CorrelationRecipe | Problem]
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
    body: CorrelationRecipeUpdate,
) -> CorrelationRecipe | Problem | None:
    """Enable or tune a recipe

     Sets `enabled` and parameter overrides; overrides survive built-in version updates.

    **Scope:** `admin`.

    Args:
        recipe_id (str):
        body (CorrelationRecipeUpdate): Operator changes to a recipe

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CorrelationRecipe | Problem
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
    body: CorrelationRecipeUpdate,
) -> Response[CorrelationRecipe | Problem]:
    """Enable or tune a recipe

     Sets `enabled` and parameter overrides; overrides survive built-in version updates.

    **Scope:** `admin`.

    Args:
        recipe_id (str):
        body (CorrelationRecipeUpdate): Operator changes to a recipe

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CorrelationRecipe | Problem]
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
    body: CorrelationRecipeUpdate,
) -> CorrelationRecipe | Problem | None:
    """Enable or tune a recipe

     Sets `enabled` and parameter overrides; overrides survive built-in version updates.

    **Scope:** `admin`.

    Args:
        recipe_id (str):
        body (CorrelationRecipeUpdate): Operator changes to a recipe

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CorrelationRecipe | Problem
    """

    return (
        await asyncio_detailed(
            recipe_id=recipe_id,
            client=client,
            body=body,
        )
    ).parsed
