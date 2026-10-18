// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'share_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ShareRequestCWProxy {
  ShareRequest grantees(List<String> grantees);

  ShareRequest role(ShareRequestRoleEnum? role);

  ShareRequest message(String? message);

  ShareRequest expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ShareRequest call({
    List<String> grantees,
    ShareRequestRoleEnum? role,
    String? message,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfShareRequest.copyWith(...)` or call `instanceOfShareRequest.copyWith.fieldName(value)` for a single field.
class _$ShareRequestCWProxyImpl implements _$ShareRequestCWProxy {
  const _$ShareRequestCWProxyImpl(this._value);

  final ShareRequest _value;

  @override
  ShareRequest grantees(List<String> grantees) => call(grantees: grantees);

  @override
  ShareRequest role(ShareRequestRoleEnum? role) => call(role: role);

  @override
  ShareRequest message(String? message) => call(message: message);

  @override
  ShareRequest expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ShareRequest call({
    Object? grantees = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return ShareRequest(
      grantees: grantees == const $CopyWithPlaceholder() || grantees == null
          ? _value.grantees
          // ignore: cast_nullable_to_non_nullable
          : grantees as List<String>,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as ShareRequestRoleEnum?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $ShareRequestCopyWith on ShareRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfShareRequest.copyWith(...)` or `instanceOfShareRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ShareRequestCWProxy get copyWith => _$ShareRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShareRequest _$ShareRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ShareRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['grantees']);
      final val = ShareRequest(
        grantees: $checkedConvert(
          'grantees',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        role: $checkedConvert(
          'role',
          (v) => $enumDecodeNullable(_$ShareRequestRoleEnumEnumMap, v),
        ),
        message: $checkedConvert('message', (v) => v as String?),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ShareRequestToJson(ShareRequest instance) =>
    <String, dynamic>{
      'grantees': instance.grantees,
      'role': ?_$ShareRequestRoleEnumEnumMap[instance.role],
      'message': ?instance.message,
      'expiresAt': ?instance.expiresAt?.toIso8601String(),
    };

const _$ShareRequestRoleEnumEnumMap = {
  ShareRequestRoleEnum.viewer: 'viewer',
  ShareRequestRoleEnum.commenter: 'commenter',
  ShareRequestRoleEnum.editor: 'editor',
};
