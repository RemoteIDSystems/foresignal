// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_suggestion.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSuggestionCWProxy {
  ChronicleSuggestion draft(ChronicleInput draft);

  ChronicleSuggestion estimate(ChronicleSuggestionEstimate? estimate);

  ChronicleSuggestion rationale(List<String>? rationale);

  ChronicleSuggestion textOrigin(ChronicleSuggestionTextOriginEnum? textOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestion(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSuggestion call({
    ChronicleInput draft,
    ChronicleSuggestionEstimate? estimate,
    List<String>? rationale,
    ChronicleSuggestionTextOriginEnum? textOrigin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSuggestion.copyWith(...)` or call `instanceOfChronicleSuggestion.copyWith.fieldName(value)` for a single field.
class _$ChronicleSuggestionCWProxyImpl implements _$ChronicleSuggestionCWProxy {
  const _$ChronicleSuggestionCWProxyImpl(this._value);

  final ChronicleSuggestion _value;

  @override
  ChronicleSuggestion draft(ChronicleInput draft) => call(draft: draft);

  @override
  ChronicleSuggestion estimate(ChronicleSuggestionEstimate? estimate) =>
      call(estimate: estimate);

  @override
  ChronicleSuggestion rationale(List<String>? rationale) =>
      call(rationale: rationale);

  @override
  ChronicleSuggestion textOrigin(
    ChronicleSuggestionTextOriginEnum? textOrigin,
  ) => call(textOrigin: textOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSuggestion call({
    Object? draft = const $CopyWithPlaceholder(),
    Object? estimate = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSuggestion(
      draft: draft == const $CopyWithPlaceholder() || draft == null
          ? _value.draft
          // ignore: cast_nullable_to_non_nullable
          : draft as ChronicleInput,
      estimate: estimate == const $CopyWithPlaceholder()
          ? _value.estimate
          // ignore: cast_nullable_to_non_nullable
          : estimate as ChronicleSuggestionEstimate?,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as List<String>?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ChronicleSuggestionTextOriginEnum?,
    );
  }
}

extension $ChronicleSuggestionCopyWith on ChronicleSuggestion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSuggestion.copyWith(...)` or `instanceOfChronicleSuggestion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSuggestionCWProxy get copyWith =>
      _$ChronicleSuggestionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSuggestion _$ChronicleSuggestionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSuggestion', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['draft']);
      final val = ChronicleSuggestion(
        draft: $checkedConvert(
          'draft',
          (v) => ChronicleInput.fromJson(v as Map<String, dynamic>),
        ),
        estimate: $checkedConvert(
          'estimate',
          (v) => v == null
              ? null
              : ChronicleSuggestionEstimate.fromJson(v as Map<String, dynamic>),
        ),
        rationale: $checkedConvert(
          'rationale',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) => $enumDecodeNullable(
            _$ChronicleSuggestionTextOriginEnumEnumMap,
            v,
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSuggestionToJson(
  ChronicleSuggestion instance,
) => <String, dynamic>{
  'draft': instance.draft.toJson(),
  'estimate': ?instance.estimate?.toJson(),
  'rationale': ?instance.rationale,
  'textOrigin':
      ?_$ChronicleSuggestionTextOriginEnumEnumMap[instance.textOrigin],
};

const _$ChronicleSuggestionTextOriginEnumEnumMap = {
  ChronicleSuggestionTextOriginEnum.deterministic: 'deterministic',
  ChronicleSuggestionTextOriginEnum.llm: 'llm',
};
