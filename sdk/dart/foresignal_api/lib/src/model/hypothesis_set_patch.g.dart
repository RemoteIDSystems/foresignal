// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetPatchCWProxy {
  HypothesisSetPatch description(String? description);

  HypothesisSetPatch cadence(HypothesisSetPatchCadenceEnum? cadence);

  HypothesisSetPatch status(HypothesisSetPatchStatusEnum? status);

  HypothesisSetPatch version(int version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetPatch call({
    String? description,
    HypothesisSetPatchCadenceEnum? cadence,
    HypothesisSetPatchStatusEnum? status,
    int version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetPatch.copyWith(...)` or call `instanceOfHypothesisSetPatch.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetPatchCWProxyImpl implements _$HypothesisSetPatchCWProxy {
  const _$HypothesisSetPatchCWProxyImpl(this._value);

  final HypothesisSetPatch _value;

  @override
  HypothesisSetPatch description(String? description) =>
      call(description: description);

  @override
  HypothesisSetPatch cadence(HypothesisSetPatchCadenceEnum? cadence) =>
      call(cadence: cadence);

  @override
  HypothesisSetPatch status(HypothesisSetPatchStatusEnum? status) =>
      call(status: status);

  @override
  HypothesisSetPatch version(int version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetPatch call({
    Object? description = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetPatch(
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      cadence: cadence == const $CopyWithPlaceholder()
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as HypothesisSetPatchCadenceEnum?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HypothesisSetPatchStatusEnum?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
    );
  }
}

extension $HypothesisSetPatchCopyWith on HypothesisSetPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetPatch.copyWith(...)` or `instanceOfHypothesisSetPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetPatchCWProxy get copyWith =>
      _$HypothesisSetPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetPatch _$HypothesisSetPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisSetPatch', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['version']);
      final val = HypothesisSetPatch(
        description: $checkedConvert('description', (v) => v as String?),
        cadence: $checkedConvert(
          'cadence',
          (v) => $enumDecodeNullable(_$HypothesisSetPatchCadenceEnumEnumMap, v),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$HypothesisSetPatchStatusEnumEnumMap, v),
        ),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisSetPatchToJson(HypothesisSetPatch instance) =>
    <String, dynamic>{
      'description': ?instance.description,
      'cadence': ?_$HypothesisSetPatchCadenceEnumEnumMap[instance.cadence],
      'status': ?_$HypothesisSetPatchStatusEnumEnumMap[instance.status],
      'version': instance.version,
    };

const _$HypothesisSetPatchCadenceEnumEnumMap = {
  HypothesisSetPatchCadenceEnum.daily: 'daily',
  HypothesisSetPatchCadenceEnum.weekly: 'weekly',
  HypothesisSetPatchCadenceEnum.monthly: 'monthly',
};

const _$HypothesisSetPatchStatusEnumEnumMap = {
  HypothesisSetPatchStatusEnum.active: 'active',
  HypothesisSetPatchStatusEnum.archived: 'archived',
};
