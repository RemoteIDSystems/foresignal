// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'contract.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContractCWProxy {
  Contract contractVersion(String contractVersion);

  Contract objectTypes(List<String> objectTypes);

  Contract relationTypes(List<String> relationTypes);

  Contract contextSchemaVersion(int contextSchemaVersion);

  Contract deepLinkVersion(int deepLinkVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Contract(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Contract(...).copyWith(id: 12, name: "My name")
  /// ```
  Contract call({
    String contractVersion,
    List<String> objectTypes,
    List<String> relationTypes,
    int contextSchemaVersion,
    int deepLinkVersion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContract.copyWith(...)` or call `instanceOfContract.copyWith.fieldName(value)` for a single field.
class _$ContractCWProxyImpl implements _$ContractCWProxy {
  const _$ContractCWProxyImpl(this._value);

  final Contract _value;

  @override
  Contract contractVersion(String contractVersion) =>
      call(contractVersion: contractVersion);

  @override
  Contract objectTypes(List<String> objectTypes) =>
      call(objectTypes: objectTypes);

  @override
  Contract relationTypes(List<String> relationTypes) =>
      call(relationTypes: relationTypes);

  @override
  Contract contextSchemaVersion(int contextSchemaVersion) =>
      call(contextSchemaVersion: contextSchemaVersion);

  @override
  Contract deepLinkVersion(int deepLinkVersion) =>
      call(deepLinkVersion: deepLinkVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Contract(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Contract(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Contract call({
    Object? contractVersion = const $CopyWithPlaceholder(),
    Object? objectTypes = const $CopyWithPlaceholder(),
    Object? relationTypes = const $CopyWithPlaceholder(),
    Object? contextSchemaVersion = const $CopyWithPlaceholder(),
    Object? deepLinkVersion = const $CopyWithPlaceholder(),
  }) {
    return Contract(
      contractVersion:
          contractVersion == const $CopyWithPlaceholder() ||
              contractVersion == null
          ? _value.contractVersion
          // ignore: cast_nullable_to_non_nullable
          : contractVersion as String,
      objectTypes:
          objectTypes == const $CopyWithPlaceholder() || objectTypes == null
          ? _value.objectTypes
          // ignore: cast_nullable_to_non_nullable
          : objectTypes as List<String>,
      relationTypes:
          relationTypes == const $CopyWithPlaceholder() || relationTypes == null
          ? _value.relationTypes
          // ignore: cast_nullable_to_non_nullable
          : relationTypes as List<String>,
      contextSchemaVersion:
          contextSchemaVersion == const $CopyWithPlaceholder() ||
              contextSchemaVersion == null
          ? _value.contextSchemaVersion
          // ignore: cast_nullable_to_non_nullable
          : contextSchemaVersion as int,
      deepLinkVersion:
          deepLinkVersion == const $CopyWithPlaceholder() ||
              deepLinkVersion == null
          ? _value.deepLinkVersion
          // ignore: cast_nullable_to_non_nullable
          : deepLinkVersion as int,
    );
  }
}

extension $ContractCopyWith on Contract {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContract.copyWith(...)` or `instanceOfContract.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContractCWProxy get copyWith => _$ContractCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Contract _$ContractFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Contract', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'contractVersion',
          'objectTypes',
          'relationTypes',
          'contextSchemaVersion',
          'deepLinkVersion',
        ],
      );
      final val = Contract(
        contractVersion: $checkedConvert('contractVersion', (v) => v as String),
        objectTypes: $checkedConvert(
          'objectTypes',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        relationTypes: $checkedConvert(
          'relationTypes',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        contextSchemaVersion: $checkedConvert(
          'contextSchemaVersion',
          (v) => (v as num).toInt(),
        ),
        deepLinkVersion: $checkedConvert(
          'deepLinkVersion',
          (v) => (v as num).toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ContractToJson(Contract instance) => <String, dynamic>{
  'contractVersion': instance.contractVersion,
  'objectTypes': instance.objectTypes,
  'relationTypes': instance.relationTypes,
  'contextSchemaVersion': instance.contextSchemaVersion,
  'deepLinkVersion': instance.deepLinkVersion,
};
