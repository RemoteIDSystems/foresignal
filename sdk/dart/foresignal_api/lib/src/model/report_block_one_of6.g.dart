// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of6.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf6CWProxy {
  ReportBlockOneOf6 id(String? id);

  ReportBlockOneOf6 type(ReportBlockOneOf6TypeEnum type);

  ReportBlockOneOf6 title(String? title);

  ReportBlockOneOf6 textOrigin(ReportBlockOneOf6TextOriginEnum? textOrigin);

  ReportBlockOneOf6 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf6 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf6 questions(
    List<ReportBlockOneOf6AllOfQuestionsInner> questions,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf6(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf6(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf6 call({
    String? id,
    ReportBlockOneOf6TypeEnum type,
    String? title,
    ReportBlockOneOf6TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    List<ReportBlockOneOf6AllOfQuestionsInner> questions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf6.copyWith(...)` or call `instanceOfReportBlockOneOf6.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf6CWProxyImpl implements _$ReportBlockOneOf6CWProxy {
  const _$ReportBlockOneOf6CWProxyImpl(this._value);

  final ReportBlockOneOf6 _value;

  @override
  ReportBlockOneOf6 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf6 type(ReportBlockOneOf6TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf6 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf6 textOrigin(ReportBlockOneOf6TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf6 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf6 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf6 questions(
    List<ReportBlockOneOf6AllOfQuestionsInner> questions,
  ) => call(questions: questions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf6(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf6(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf6 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? questions = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf6(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf6TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf6TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      questions: questions == const $CopyWithPlaceholder() || questions == null
          ? _value.questions
          // ignore: cast_nullable_to_non_nullable
          : questions as List<ReportBlockOneOf6AllOfQuestionsInner>,
    );
  }
}

extension $ReportBlockOneOf6CopyWith on ReportBlockOneOf6 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf6.copyWith(...)` or `instanceOfReportBlockOneOf6.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf6CWProxy get copyWith =>
      _$ReportBlockOneOf6CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf6 _$ReportBlockOneOf6FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf6', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'questions']);
      final val = ReportBlockOneOf6(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf6TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf6TextOriginEnumEnumMap, v),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        updatedBy: $checkedConvert(
          'updatedBy',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        questions: $checkedConvert(
          'questions',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ReportBlockOneOf6AllOfQuestionsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf6ToJson(
  ReportBlockOneOf6 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf6TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf6TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'questions': instance.questions.map((e) => e.toJson()).toList(),
};

const _$ReportBlockOneOf6TypeEnumEnumMap = {
  ReportBlockOneOf6TypeEnum.forecasts: 'forecasts',
};

const _$ReportBlockOneOf6TextOriginEnumEnumMap = {
  ReportBlockOneOf6TextOriginEnum.author: 'author',
  ReportBlockOneOf6TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf6TextOriginEnum.llm: 'llm',
};
