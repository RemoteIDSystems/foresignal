// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf1CWProxy {
  ReportBlockOneOf1 id(String? id);

  ReportBlockOneOf1 type(ReportBlockOneOf1TypeEnum type);

  ReportBlockOneOf1 title(String? title);

  ReportBlockOneOf1 textOrigin(ReportBlockOneOf1TextOriginEnum? textOrigin);

  ReportBlockOneOf1 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf1 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf1 judgments(
    List<ReportBlockOneOf1AllOfJudgmentsInner> judgments,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf1 call({
    String? id,
    ReportBlockOneOf1TypeEnum type,
    String? title,
    ReportBlockOneOf1TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    List<ReportBlockOneOf1AllOfJudgmentsInner> judgments,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf1.copyWith(...)` or call `instanceOfReportBlockOneOf1.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf1CWProxyImpl implements _$ReportBlockOneOf1CWProxy {
  const _$ReportBlockOneOf1CWProxyImpl(this._value);

  final ReportBlockOneOf1 _value;

  @override
  ReportBlockOneOf1 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf1 type(ReportBlockOneOf1TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf1 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf1 textOrigin(ReportBlockOneOf1TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf1 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf1 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf1 judgments(
    List<ReportBlockOneOf1AllOfJudgmentsInner> judgments,
  ) => call(judgments: judgments);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf1 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? judgments = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf1(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf1TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf1TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      judgments: judgments == const $CopyWithPlaceholder() || judgments == null
          ? _value.judgments
          // ignore: cast_nullable_to_non_nullable
          : judgments as List<ReportBlockOneOf1AllOfJudgmentsInner>,
    );
  }
}

extension $ReportBlockOneOf1CopyWith on ReportBlockOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf1.copyWith(...)` or `instanceOfReportBlockOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf1CWProxy get copyWith =>
      _$ReportBlockOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf1 _$ReportBlockOneOf1FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf1', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'judgments']);
      final val = ReportBlockOneOf1(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf1TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf1TextOriginEnumEnumMap, v),
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
        judgments: $checkedConvert(
          'judgments',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ReportBlockOneOf1AllOfJudgmentsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf1ToJson(
  ReportBlockOneOf1 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf1TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf1TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'judgments': instance.judgments.map((e) => e.toJson()).toList(),
};

const _$ReportBlockOneOf1TypeEnumEnumMap = {
  ReportBlockOneOf1TypeEnum.bluf: 'bluf',
};

const _$ReportBlockOneOf1TextOriginEnumEnumMap = {
  ReportBlockOneOf1TextOriginEnum.author: 'author',
  ReportBlockOneOf1TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf1TextOriginEnum.llm: 'llm',
};
