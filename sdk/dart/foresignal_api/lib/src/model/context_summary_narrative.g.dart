// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_summary_narrative.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextSummaryNarrativeCWProxy {
  ContextSummaryNarrative text(String text);

  ContextSummaryNarrative textOrigin(
    ContextSummaryNarrativeTextOriginEnum textOrigin,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryNarrative(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryNarrative(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextSummaryNarrative call({
    String text,
    ContextSummaryNarrativeTextOriginEnum textOrigin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextSummaryNarrative.copyWith(...)` or call `instanceOfContextSummaryNarrative.copyWith.fieldName(value)` for a single field.
class _$ContextSummaryNarrativeCWProxyImpl
    implements _$ContextSummaryNarrativeCWProxy {
  const _$ContextSummaryNarrativeCWProxyImpl(this._value);

  final ContextSummaryNarrative _value;

  @override
  ContextSummaryNarrative text(String text) => call(text: text);

  @override
  ContextSummaryNarrative textOrigin(
    ContextSummaryNarrativeTextOriginEnum textOrigin,
  ) => call(textOrigin: textOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryNarrative(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryNarrative(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextSummaryNarrative call({
    Object? text = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
  }) {
    return ContextSummaryNarrative(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      textOrigin:
          textOrigin == const $CopyWithPlaceholder() || textOrigin == null
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ContextSummaryNarrativeTextOriginEnum,
    );
  }
}

extension $ContextSummaryNarrativeCopyWith on ContextSummaryNarrative {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextSummaryNarrative.copyWith(...)` or `instanceOfContextSummaryNarrative.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextSummaryNarrativeCWProxy get copyWith =>
      _$ContextSummaryNarrativeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextSummaryNarrative _$ContextSummaryNarrativeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ContextSummaryNarrative', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['text', 'textOrigin']);
  final val = ContextSummaryNarrative(
    text: $checkedConvert('text', (v) => v as String),
    textOrigin: $checkedConvert(
      'textOrigin',
      (v) => $enumDecode(_$ContextSummaryNarrativeTextOriginEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$ContextSummaryNarrativeToJson(
  ContextSummaryNarrative instance,
) => <String, dynamic>{
  'text': instance.text,
  'textOrigin':
      _$ContextSummaryNarrativeTextOriginEnumEnumMap[instance.textOrigin]!,
};

const _$ContextSummaryNarrativeTextOriginEnumEnumMap = {
  ContextSummaryNarrativeTextOriginEnum.llm: 'llm',
};
