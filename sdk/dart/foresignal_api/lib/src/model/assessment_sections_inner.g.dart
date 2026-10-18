// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_sections_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentSectionsInnerCWProxy {
  AssessmentSectionsInner key(AssessmentSectionsInnerKeyEnum key);

  AssessmentSectionsInner title(String title);

  AssessmentSectionsInner markdown(String markdown);

  AssessmentSectionsInner textOrigin(
    AssessmentSectionsInnerTextOriginEnum textOrigin,
  );

  AssessmentSectionsInner data(Object? data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentSectionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentSectionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentSectionsInner call({
    AssessmentSectionsInnerKeyEnum key,
    String title,
    String markdown,
    AssessmentSectionsInnerTextOriginEnum textOrigin,
    Object? data,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentSectionsInner.copyWith(...)` or call `instanceOfAssessmentSectionsInner.copyWith.fieldName(value)` for a single field.
class _$AssessmentSectionsInnerCWProxyImpl
    implements _$AssessmentSectionsInnerCWProxy {
  const _$AssessmentSectionsInnerCWProxyImpl(this._value);

  final AssessmentSectionsInner _value;

  @override
  AssessmentSectionsInner key(AssessmentSectionsInnerKeyEnum key) =>
      call(key: key);

  @override
  AssessmentSectionsInner title(String title) => call(title: title);

  @override
  AssessmentSectionsInner markdown(String markdown) => call(markdown: markdown);

  @override
  AssessmentSectionsInner textOrigin(
    AssessmentSectionsInnerTextOriginEnum textOrigin,
  ) => call(textOrigin: textOrigin);

  @override
  AssessmentSectionsInner data(Object? data) => call(data: data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentSectionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentSectionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentSectionsInner call({
    Object? key = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? markdown = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
  }) {
    return AssessmentSectionsInner(
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as AssessmentSectionsInnerKeyEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      markdown: markdown == const $CopyWithPlaceholder() || markdown == null
          ? _value.markdown
          // ignore: cast_nullable_to_non_nullable
          : markdown as String,
      textOrigin:
          textOrigin == const $CopyWithPlaceholder() || textOrigin == null
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as AssessmentSectionsInnerTextOriginEnum,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Object?,
    );
  }
}

extension $AssessmentSectionsInnerCopyWith on AssessmentSectionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentSectionsInner.copyWith(...)` or `instanceOfAssessmentSectionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentSectionsInnerCWProxy get copyWith =>
      _$AssessmentSectionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentSectionsInner _$AssessmentSectionsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AssessmentSectionsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['key', 'title', 'markdown', 'textOrigin'],
  );
  final val = AssessmentSectionsInner(
    key: $checkedConvert(
      'key',
      (v) => $enumDecode(_$AssessmentSectionsInnerKeyEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    markdown: $checkedConvert('markdown', (v) => v as String),
    textOrigin: $checkedConvert(
      'textOrigin',
      (v) => $enumDecode(_$AssessmentSectionsInnerTextOriginEnumEnumMap, v),
    ),
    data: $checkedConvert('data', (v) => v),
  );
  return val;
});

Map<String, dynamic> _$AssessmentSectionsInnerToJson(
  AssessmentSectionsInner instance,
) => <String, dynamic>{
  'key': _$AssessmentSectionsInnerKeyEnumEnumMap[instance.key]!,
  'title': instance.title,
  'markdown': instance.markdown,
  'textOrigin':
      _$AssessmentSectionsInnerTextOriginEnumEnumMap[instance.textOrigin]!,
  'data': ?instance.data,
};

const _$AssessmentSectionsInnerKeyEnumEnumMap = {
  AssessmentSectionsInnerKeyEnum.bluf: 'bluf',
  AssessmentSectionsInnerKeyEnum.whatChanged: 'what-changed',
  AssessmentSectionsInnerKeyEnum.situations: 'situations',
  AssessmentSectionsInnerKeyEnum.hypotheses: 'hypotheses',
  AssessmentSectionsInnerKeyEnum.indicators: 'indicators',
  AssessmentSectionsInnerKeyEnum.forecasts: 'forecasts',
  AssessmentSectionsInnerKeyEnum.speculation: 'speculation',
  AssessmentSectionsInnerKeyEnum.sources: 'sources',
  AssessmentSectionsInnerKeyEnum.collectionGaps: 'collection-gaps',
  AssessmentSectionsInnerKeyEnum.keyInsights: 'key-insights',
  AssessmentSectionsInnerKeyEnum.outlook: 'outlook',
  AssessmentSectionsInnerKeyEnum.risks: 'risks',
  AssessmentSectionsInnerKeyEnum.social: 'social',
};

const _$AssessmentSectionsInnerTextOriginEnumEnumMap = {
  AssessmentSectionsInnerTextOriginEnum.deterministic: 'deterministic',
  AssessmentSectionsInnerTextOriginEnum.llm: 'llm',
  AssessmentSectionsInnerTextOriginEnum.mixed: 'mixed',
};
