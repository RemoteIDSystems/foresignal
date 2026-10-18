// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'access_grant_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AccessGrantInputCWProxy {
  AccessGrantInput role(AccessGrantInputRoleEnum? role);

  AccessGrantInput expiresAt(DateTime? expiresAt);

  AccessGrantInput deliverAs(AccessGrantInputDeliverAsEnum? deliverAs);

  AccessGrantInput message(String? message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessGrantInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessGrantInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AccessGrantInput call({
    AccessGrantInputRoleEnum? role,
    DateTime? expiresAt,
    AccessGrantInputDeliverAsEnum? deliverAs,
    String? message,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAccessGrantInput.copyWith(...)` or call `instanceOfAccessGrantInput.copyWith.fieldName(value)` for a single field.
class _$AccessGrantInputCWProxyImpl implements _$AccessGrantInputCWProxy {
  const _$AccessGrantInputCWProxyImpl(this._value);

  final AccessGrantInput _value;

  @override
  AccessGrantInput role(AccessGrantInputRoleEnum? role) => call(role: role);

  @override
  AccessGrantInput expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  AccessGrantInput deliverAs(AccessGrantInputDeliverAsEnum? deliverAs) =>
      call(deliverAs: deliverAs);

  @override
  AccessGrantInput message(String? message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessGrantInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessGrantInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AccessGrantInput call({
    Object? role = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? deliverAs = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return AccessGrantInput(
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as AccessGrantInputRoleEnum?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      deliverAs: deliverAs == const $CopyWithPlaceholder()
          ? _value.deliverAs
          // ignore: cast_nullable_to_non_nullable
          : deliverAs as AccessGrantInputDeliverAsEnum?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
    );
  }
}

extension $AccessGrantInputCopyWith on AccessGrantInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAccessGrantInput.copyWith(...)` or `instanceOfAccessGrantInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AccessGrantInputCWProxy get copyWith => _$AccessGrantInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccessGrantInput _$AccessGrantInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AccessGrantInput', json, ($checkedConvert) {
      final val = AccessGrantInput(
        role: $checkedConvert(
          'role',
          (v) => $enumDecodeNullable(_$AccessGrantInputRoleEnumEnumMap, v),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        deliverAs: $checkedConvert(
          'deliverAs',
          (v) => $enumDecodeNullable(_$AccessGrantInputDeliverAsEnumEnumMap, v),
        ),
        message: $checkedConvert('message', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AccessGrantInputToJson(AccessGrantInput instance) =>
    <String, dynamic>{
      'role': ?_$AccessGrantInputRoleEnumEnumMap[instance.role],
      'expiresAt': ?instance.expiresAt?.toIso8601String(),
      'deliverAs': ?_$AccessGrantInputDeliverAsEnumEnumMap[instance.deliverAs],
      'message': ?instance.message,
    };

const _$AccessGrantInputRoleEnumEnumMap = {
  AccessGrantInputRoleEnum.viewer: 'viewer',
  AccessGrantInputRoleEnum.commenter: 'commenter',
};

const _$AccessGrantInputDeliverAsEnumEnumMap = {
  AccessGrantInputDeliverAsEnum.report: 'report',
  AccessGrantInputDeliverAsEnum.chronicleExtract: 'chronicle-extract',
};
