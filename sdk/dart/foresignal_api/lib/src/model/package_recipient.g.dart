// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'package_recipient.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackageRecipientCWProxy {
  PackageRecipient instanceId(String instanceId);

  PackageRecipient principals(List<String> principals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageRecipient(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageRecipient(...).copyWith(id: 12, name: "My name")
  /// ```
  PackageRecipient call({String instanceId, List<String> principals});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackageRecipient.copyWith(...)` or call `instanceOfPackageRecipient.copyWith.fieldName(value)` for a single field.
class _$PackageRecipientCWProxyImpl implements _$PackageRecipientCWProxy {
  const _$PackageRecipientCWProxyImpl(this._value);

  final PackageRecipient _value;

  @override
  PackageRecipient instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  PackageRecipient principals(List<String> principals) =>
      call(principals: principals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageRecipient(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageRecipient(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackageRecipient call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? principals = const $CopyWithPlaceholder(),
  }) {
    return PackageRecipient(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      principals:
          principals == const $CopyWithPlaceholder() || principals == null
          ? _value.principals
          // ignore: cast_nullable_to_non_nullable
          : principals as List<String>,
    );
  }
}

extension $PackageRecipientCopyWith on PackageRecipient {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackageRecipient.copyWith(...)` or `instanceOfPackageRecipient.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackageRecipientCWProxy get copyWith => _$PackageRecipientCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackageRecipient _$PackageRecipientFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PackageRecipient', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['instanceId', 'principals']);
      final val = PackageRecipient(
        instanceId: $checkedConvert('instanceId', (v) => v as String),
        principals: $checkedConvert(
          'principals',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PackageRecipientToJson(PackageRecipient instance) =>
    <String, dynamic>{
      'instanceId': instance.instanceId,
      'principals': instance.principals,
    };
