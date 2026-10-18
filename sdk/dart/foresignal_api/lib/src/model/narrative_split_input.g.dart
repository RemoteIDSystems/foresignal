// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_split_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeSplitInputCWProxy {
  NarrativeSplitInput postRefs(List<String> postRefs);

  NarrativeSplitInput reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeSplitInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeSplitInput(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativeSplitInput call({List<String> postRefs, String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativeSplitInput.copyWith(...)` or call `instanceOfNarrativeSplitInput.copyWith.fieldName(value)` for a single field.
class _$NarrativeSplitInputCWProxyImpl implements _$NarrativeSplitInputCWProxy {
  const _$NarrativeSplitInputCWProxyImpl(this._value);

  final NarrativeSplitInput _value;

  @override
  NarrativeSplitInput postRefs(List<String> postRefs) =>
      call(postRefs: postRefs);

  @override
  NarrativeSplitInput reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeSplitInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeSplitInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativeSplitInput call({
    Object? postRefs = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return NarrativeSplitInput(
      postRefs: postRefs == const $CopyWithPlaceholder() || postRefs == null
          ? _value.postRefs
          // ignore: cast_nullable_to_non_nullable
          : postRefs as List<String>,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $NarrativeSplitInputCopyWith on NarrativeSplitInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativeSplitInput.copyWith(...)` or `instanceOfNarrativeSplitInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeSplitInputCWProxy get copyWith =>
      _$NarrativeSplitInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativeSplitInput _$NarrativeSplitInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativeSplitInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['postRefs']);
      final val = NarrativeSplitInput(
        postRefs: $checkedConvert(
          'postRefs',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NarrativeSplitInputToJson(
  NarrativeSplitInput instance,
) => <String, dynamic>{
  'postRefs': instance.postRefs,
  'reason': ?instance.reason,
};
