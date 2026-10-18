// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of10.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf10CWProxy {
  ReportBlockOneOf10 id(String? id);

  ReportBlockOneOf10 type(ReportBlockOneOf10TypeEnum type);

  ReportBlockOneOf10 title(String? title);

  ReportBlockOneOf10 textOrigin(ReportBlockOneOf10TextOriginEnum? textOrigin);

  ReportBlockOneOf10 updatedAt(DateTime? updatedAt);

  ReportBlockOneOf10 updatedBy(PrincipalSummary? updatedBy);

  ReportBlockOneOf10 attributions(
    List<ReportBlockOneOf10AllOfAttributionsInner> attributions,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf10(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf10(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf10 call({
    String? id,
    ReportBlockOneOf10TypeEnum type,
    String? title,
    ReportBlockOneOf10TextOriginEnum? textOrigin,
    DateTime? updatedAt,
    PrincipalSummary? updatedBy,
    List<ReportBlockOneOf10AllOfAttributionsInner> attributions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf10.copyWith(...)` or call `instanceOfReportBlockOneOf10.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf10CWProxyImpl implements _$ReportBlockOneOf10CWProxy {
  const _$ReportBlockOneOf10CWProxyImpl(this._value);

  final ReportBlockOneOf10 _value;

  @override
  ReportBlockOneOf10 id(String? id) => call(id: id);

  @override
  ReportBlockOneOf10 type(ReportBlockOneOf10TypeEnum type) => call(type: type);

  @override
  ReportBlockOneOf10 title(String? title) => call(title: title);

  @override
  ReportBlockOneOf10 textOrigin(ReportBlockOneOf10TextOriginEnum? textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  ReportBlockOneOf10 updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  ReportBlockOneOf10 updatedBy(PrincipalSummary? updatedBy) =>
      call(updatedBy: updatedBy);

  @override
  ReportBlockOneOf10 attributions(
    List<ReportBlockOneOf10AllOfAttributionsInner> attributions,
  ) => call(attributions: attributions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf10(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf10(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf10 call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? updatedBy = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf10(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportBlockOneOf10TypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ReportBlockOneOf10TextOriginEnum?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      updatedBy: updatedBy == const $CopyWithPlaceholder()
          ? _value.updatedBy
          // ignore: cast_nullable_to_non_nullable
          : updatedBy as PrincipalSummary?,
      attributions:
          attributions == const $CopyWithPlaceholder() || attributions == null
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<ReportBlockOneOf10AllOfAttributionsInner>,
    );
  }
}

extension $ReportBlockOneOf10CopyWith on ReportBlockOneOf10 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf10.copyWith(...)` or `instanceOfReportBlockOneOf10.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf10CWProxy get copyWith =>
      _$ReportBlockOneOf10CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf10 _$ReportBlockOneOf10FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf10', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'attributions']);
      final val = ReportBlockOneOf10(
        id: $checkedConvert('id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportBlockOneOf10TypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        textOrigin: $checkedConvert(
          'textOrigin',
          (v) =>
              $enumDecodeNullable(_$ReportBlockOneOf10TextOriginEnumEnumMap, v),
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
        attributions: $checkedConvert(
          'attributions',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ReportBlockOneOf10AllOfAttributionsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf10ToJson(
  ReportBlockOneOf10 instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'type': _$ReportBlockOneOf10TypeEnumEnumMap[instance.type]!,
  'title': ?instance.title,
  'textOrigin': ?_$ReportBlockOneOf10TextOriginEnumEnumMap[instance.textOrigin],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'updatedBy': ?instance.updatedBy?.toJson(),
  'attributions': instance.attributions.map((e) => e.toJson()).toList(),
};

const _$ReportBlockOneOf10TypeEnumEnumMap = {
  ReportBlockOneOf10TypeEnum.sources: 'sources',
};

const _$ReportBlockOneOf10TextOriginEnumEnumMap = {
  ReportBlockOneOf10TextOriginEnum.author: 'author',
  ReportBlockOneOf10TextOriginEnum.deterministic: 'deterministic',
  ReportBlockOneOf10TextOriginEnum.llm: 'llm',
};
