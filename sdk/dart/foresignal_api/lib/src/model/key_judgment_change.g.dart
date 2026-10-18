// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'key_judgment_change.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KeyJudgmentChangeCWProxy {
  KeyJudgmentChange previousProbability(num? previousProbability);

  KeyJudgmentChange previousText(String? previousText);

  KeyJudgmentChange explanation(String explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyJudgmentChange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyJudgmentChange(...).copyWith(id: 12, name: "My name")
  /// ```
  KeyJudgmentChange call({
    num? previousProbability,
    String? previousText,
    String explanation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKeyJudgmentChange.copyWith(...)` or call `instanceOfKeyJudgmentChange.copyWith.fieldName(value)` for a single field.
class _$KeyJudgmentChangeCWProxyImpl implements _$KeyJudgmentChangeCWProxy {
  const _$KeyJudgmentChangeCWProxyImpl(this._value);

  final KeyJudgmentChange _value;

  @override
  KeyJudgmentChange previousProbability(num? previousProbability) =>
      call(previousProbability: previousProbability);

  @override
  KeyJudgmentChange previousText(String? previousText) =>
      call(previousText: previousText);

  @override
  KeyJudgmentChange explanation(String explanation) =>
      call(explanation: explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyJudgmentChange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyJudgmentChange(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KeyJudgmentChange call({
    Object? previousProbability = const $CopyWithPlaceholder(),
    Object? previousText = const $CopyWithPlaceholder(),
    Object? explanation = const $CopyWithPlaceholder(),
  }) {
    return KeyJudgmentChange(
      previousProbability: previousProbability == const $CopyWithPlaceholder()
          ? _value.previousProbability
          // ignore: cast_nullable_to_non_nullable
          : previousProbability as num?,
      previousText: previousText == const $CopyWithPlaceholder()
          ? _value.previousText
          // ignore: cast_nullable_to_non_nullable
          : previousText as String?,
      explanation:
          explanation == const $CopyWithPlaceholder() || explanation == null
          ? _value.explanation
          // ignore: cast_nullable_to_non_nullable
          : explanation as String,
    );
  }
}

extension $KeyJudgmentChangeCopyWith on KeyJudgmentChange {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKeyJudgmentChange.copyWith(...)` or `instanceOfKeyJudgmentChange.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KeyJudgmentChangeCWProxy get copyWith =>
      _$KeyJudgmentChangeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KeyJudgmentChange _$KeyJudgmentChangeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('KeyJudgmentChange', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['explanation']);
      final val = KeyJudgmentChange(
        previousProbability: $checkedConvert(
          'previousProbability',
          (v) => v as num?,
        ),
        previousText: $checkedConvert('previousText', (v) => v as String?),
        explanation: $checkedConvert('explanation', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$KeyJudgmentChangeToJson(KeyJudgmentChange instance) =>
    <String, dynamic>{
      'previousProbability': ?instance.previousProbability,
      'previousText': ?instance.previousText,
      'explanation': instance.explanation,
    };
