// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_merge_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationMergeInputCWProxy {
  SituationMergeInput intoId(String intoId);

  SituationMergeInput version(int? version);

  SituationMergeInput reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationMergeInput call({String intoId, int? version, String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationMergeInput.copyWith(...)` or call `instanceOfSituationMergeInput.copyWith.fieldName(value)` for a single field.
class _$SituationMergeInputCWProxyImpl implements _$SituationMergeInputCWProxy {
  const _$SituationMergeInputCWProxyImpl(this._value);

  final SituationMergeInput _value;

  @override
  SituationMergeInput intoId(String intoId) => call(intoId: intoId);

  @override
  SituationMergeInput version(int? version) => call(version: version);

  @override
  SituationMergeInput reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationMergeInput call({
    Object? intoId = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return SituationMergeInput(
      intoId: intoId == const $CopyWithPlaceholder() || intoId == null
          ? _value.intoId
          // ignore: cast_nullable_to_non_nullable
          : intoId as String,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $SituationMergeInputCopyWith on SituationMergeInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationMergeInput.copyWith(...)` or `instanceOfSituationMergeInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationMergeInputCWProxy get copyWith =>
      _$SituationMergeInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationMergeInput _$SituationMergeInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationMergeInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['intoId']);
      final val = SituationMergeInput(
        intoId: $checkedConvert('intoId', (v) => v as String),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SituationMergeInputToJson(
  SituationMergeInput instance,
) => <String, dynamic>{
  'intoId': instance.intoId,
  'version': ?instance.version,
  'reason': ?instance.reason,
};
