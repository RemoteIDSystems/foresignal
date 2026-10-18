// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_revision.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportRevisionCWProxy {
  ReportRevision reportId(String reportId);

  ReportRevision revision(int revision);

  ReportRevision title(String title);

  ReportRevision tlp(Tlp tlp);

  ReportRevision status(ReportRevisionStatusEnum status);

  ReportRevision publishedAt(DateTime publishedAt);

  ReportRevision publishedBy(PrincipalSummary? publishedBy);

  ReportRevision retractedAt(DateTime? retractedAt);

  ReportRevision retractionReason(String? retractionReason);

  ReportRevision manifestId(String? manifestId);

  ReportRevision pinnedRefs(List<String>? pinnedRefs);

  ReportRevision bundleSha256(String? bundleSha256);

  ReportRevision renditions(ReportRevisionRenditions? renditions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRevision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRevision(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportRevision call({
    String reportId,
    int revision,
    String title,
    Tlp tlp,
    ReportRevisionStatusEnum status,
    DateTime publishedAt,
    PrincipalSummary? publishedBy,
    DateTime? retractedAt,
    String? retractionReason,
    String? manifestId,
    List<String>? pinnedRefs,
    String? bundleSha256,
    ReportRevisionRenditions? renditions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportRevision.copyWith(...)` or call `instanceOfReportRevision.copyWith.fieldName(value)` for a single field.
class _$ReportRevisionCWProxyImpl implements _$ReportRevisionCWProxy {
  const _$ReportRevisionCWProxyImpl(this._value);

  final ReportRevision _value;

  @override
  ReportRevision reportId(String reportId) => call(reportId: reportId);

  @override
  ReportRevision revision(int revision) => call(revision: revision);

  @override
  ReportRevision title(String title) => call(title: title);

  @override
  ReportRevision tlp(Tlp tlp) => call(tlp: tlp);

  @override
  ReportRevision status(ReportRevisionStatusEnum status) =>
      call(status: status);

  @override
  ReportRevision publishedAt(DateTime publishedAt) =>
      call(publishedAt: publishedAt);

  @override
  ReportRevision publishedBy(PrincipalSummary? publishedBy) =>
      call(publishedBy: publishedBy);

  @override
  ReportRevision retractedAt(DateTime? retractedAt) =>
      call(retractedAt: retractedAt);

  @override
  ReportRevision retractionReason(String? retractionReason) =>
      call(retractionReason: retractionReason);

  @override
  ReportRevision manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  ReportRevision pinnedRefs(List<String>? pinnedRefs) =>
      call(pinnedRefs: pinnedRefs);

  @override
  ReportRevision bundleSha256(String? bundleSha256) =>
      call(bundleSha256: bundleSha256);

  @override
  ReportRevision renditions(ReportRevisionRenditions? renditions) =>
      call(renditions: renditions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRevision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRevision(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportRevision call({
    Object? reportId = const $CopyWithPlaceholder(),
    Object? revision = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? publishedAt = const $CopyWithPlaceholder(),
    Object? publishedBy = const $CopyWithPlaceholder(),
    Object? retractedAt = const $CopyWithPlaceholder(),
    Object? retractionReason = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? pinnedRefs = const $CopyWithPlaceholder(),
    Object? bundleSha256 = const $CopyWithPlaceholder(),
    Object? renditions = const $CopyWithPlaceholder(),
  }) {
    return ReportRevision(
      reportId: reportId == const $CopyWithPlaceholder() || reportId == null
          ? _value.reportId
          // ignore: cast_nullable_to_non_nullable
          : reportId as String,
      revision: revision == const $CopyWithPlaceholder() || revision == null
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ReportRevisionStatusEnum,
      publishedAt:
          publishedAt == const $CopyWithPlaceholder() || publishedAt == null
          ? _value.publishedAt
          // ignore: cast_nullable_to_non_nullable
          : publishedAt as DateTime,
      publishedBy: publishedBy == const $CopyWithPlaceholder()
          ? _value.publishedBy
          // ignore: cast_nullable_to_non_nullable
          : publishedBy as PrincipalSummary?,
      retractedAt: retractedAt == const $CopyWithPlaceholder()
          ? _value.retractedAt
          // ignore: cast_nullable_to_non_nullable
          : retractedAt as DateTime?,
      retractionReason: retractionReason == const $CopyWithPlaceholder()
          ? _value.retractionReason
          // ignore: cast_nullable_to_non_nullable
          : retractionReason as String?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      pinnedRefs: pinnedRefs == const $CopyWithPlaceholder()
          ? _value.pinnedRefs
          // ignore: cast_nullable_to_non_nullable
          : pinnedRefs as List<String>?,
      bundleSha256: bundleSha256 == const $CopyWithPlaceholder()
          ? _value.bundleSha256
          // ignore: cast_nullable_to_non_nullable
          : bundleSha256 as String?,
      renditions: renditions == const $CopyWithPlaceholder()
          ? _value.renditions
          // ignore: cast_nullable_to_non_nullable
          : renditions as ReportRevisionRenditions?,
    );
  }
}

extension $ReportRevisionCopyWith on ReportRevision {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportRevision.copyWith(...)` or `instanceOfReportRevision.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportRevisionCWProxy get copyWith => _$ReportRevisionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportRevision _$ReportRevisionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportRevision', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'reportId',
          'revision',
          'title',
          'tlp',
          'status',
          'publishedAt',
        ],
      );
      final val = ReportRevision(
        reportId: $checkedConvert('reportId', (v) => v as String),
        revision: $checkedConvert('revision', (v) => (v as num).toInt()),
        title: $checkedConvert('title', (v) => v as String),
        tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ReportRevisionStatusEnumEnumMap, v),
        ),
        publishedAt: $checkedConvert(
          'publishedAt',
          (v) => DateTime.parse(v as String),
        ),
        publishedBy: $checkedConvert(
          'publishedBy',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        retractedAt: $checkedConvert(
          'retractedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        retractionReason: $checkedConvert(
          'retractionReason',
          (v) => v as String?,
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        pinnedRefs: $checkedConvert(
          'pinnedRefs',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bundleSha256: $checkedConvert('bundleSha256', (v) => v as String?),
        renditions: $checkedConvert(
          'renditions',
          (v) => v == null
              ? null
              : ReportRevisionRenditions.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportRevisionToJson(ReportRevision instance) =>
    <String, dynamic>{
      'reportId': instance.reportId,
      'revision': instance.revision,
      'title': instance.title,
      'tlp': _$TlpEnumMap[instance.tlp]!,
      'status': _$ReportRevisionStatusEnumEnumMap[instance.status]!,
      'publishedAt': instance.publishedAt.toIso8601String(),
      'publishedBy': ?instance.publishedBy?.toJson(),
      'retractedAt': ?instance.retractedAt?.toIso8601String(),
      'retractionReason': ?instance.retractionReason,
      'manifestId': ?instance.manifestId,
      'pinnedRefs': ?instance.pinnedRefs,
      'bundleSha256': ?instance.bundleSha256,
      'renditions': ?instance.renditions?.toJson(),
    };

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$ReportRevisionStatusEnumEnumMap = {
  ReportRevisionStatusEnum.published: 'published',
  ReportRevisionStatusEnum.retracted: 'retracted',
};
