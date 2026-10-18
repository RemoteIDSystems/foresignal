// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_link_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextLinkRequestCWProxy {
  ContextLinkRequest context(WorkspaceContext context);

  ContextLinkRequest workspaceId(String? workspaceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextLinkRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextLinkRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextLinkRequest call({WorkspaceContext context, String? workspaceId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextLinkRequest.copyWith(...)` or call `instanceOfContextLinkRequest.copyWith.fieldName(value)` for a single field.
class _$ContextLinkRequestCWProxyImpl implements _$ContextLinkRequestCWProxy {
  const _$ContextLinkRequestCWProxyImpl(this._value);

  final ContextLinkRequest _value;

  @override
  ContextLinkRequest context(WorkspaceContext context) =>
      call(context: context);

  @override
  ContextLinkRequest workspaceId(String? workspaceId) =>
      call(workspaceId: workspaceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextLinkRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextLinkRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextLinkRequest call({
    Object? context = const $CopyWithPlaceholder(),
    Object? workspaceId = const $CopyWithPlaceholder(),
  }) {
    return ContextLinkRequest(
      context: context == const $CopyWithPlaceholder() || context == null
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as WorkspaceContext,
      workspaceId: workspaceId == const $CopyWithPlaceholder()
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String?,
    );
  }
}

extension $ContextLinkRequestCopyWith on ContextLinkRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextLinkRequest.copyWith(...)` or `instanceOfContextLinkRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextLinkRequestCWProxy get copyWith =>
      _$ContextLinkRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextLinkRequest _$ContextLinkRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ContextLinkRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['context']);
      final val = ContextLinkRequest(
        context: $checkedConvert(
          'context',
          (v) => WorkspaceContext.fromJson(v as Map<String, dynamic>),
        ),
        workspaceId: $checkedConvert('workspaceId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ContextLinkRequestToJson(ContextLinkRequest instance) =>
    <String, dynamic>{
      'context': instance.context.toJson(),
      'workspaceId': ?instance.workspaceId,
    };
