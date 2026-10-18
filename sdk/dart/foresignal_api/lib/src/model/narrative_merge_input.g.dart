// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_merge_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeMergeInputCWProxy {
  NarrativeMergeInput narrativeIds(List<String> narrativeIds);

  NarrativeMergeInput reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativeMergeInput call({List<String> narrativeIds, String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativeMergeInput.copyWith(...)` or call `instanceOfNarrativeMergeInput.copyWith.fieldName(value)` for a single field.
class _$NarrativeMergeInputCWProxyImpl implements _$NarrativeMergeInputCWProxy {
  const _$NarrativeMergeInputCWProxyImpl(this._value);

  final NarrativeMergeInput _value;

  @override
  NarrativeMergeInput narrativeIds(List<String> narrativeIds) =>
      call(narrativeIds: narrativeIds);

  @override
  NarrativeMergeInput reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeMergeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeMergeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativeMergeInput call({
    Object? narrativeIds = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return NarrativeMergeInput(
      narrativeIds:
          narrativeIds == const $CopyWithPlaceholder() || narrativeIds == null
          ? _value.narrativeIds
          // ignore: cast_nullable_to_non_nullable
          : narrativeIds as List<String>,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $NarrativeMergeInputCopyWith on NarrativeMergeInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativeMergeInput.copyWith(...)` or `instanceOfNarrativeMergeInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeMergeInputCWProxy get copyWith =>
      _$NarrativeMergeInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativeMergeInput _$NarrativeMergeInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativeMergeInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['narrativeIds']);
      final val = NarrativeMergeInput(
        narrativeIds: $checkedConvert(
          'narrativeIds',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NarrativeMergeInputToJson(
  NarrativeMergeInput instance,
) => <String, dynamic>{
  'narrativeIds': instance.narrativeIds,
  'reason': ?instance.reason,
};
