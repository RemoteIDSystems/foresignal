// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_bundle.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBundleCWProxy {
  ReportBundle report(Report report);

  ReportBundle revision(int revision);

  ReportBundle tlp(Tlp tlp);

  ReportBundle envelopes(List<ObjectEnvelope> envelopes);

  ReportBundle snapshots(Map<String, Object>? snapshots);

  ReportBundle attachments(List<ReportBundleAttachmentsInner>? attachments);

  ReportBundle manifestId(String? manifestId);

  ReportBundle missing(List<String>? missing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBundle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBundle(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBundle call({
    Report report,
    int revision,
    Tlp tlp,
    List<ObjectEnvelope> envelopes,
    Map<String, Object>? snapshots,
    List<ReportBundleAttachmentsInner>? attachments,
    String? manifestId,
    List<String>? missing,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBundle.copyWith(...)` or call `instanceOfReportBundle.copyWith.fieldName(value)` for a single field.
class _$ReportBundleCWProxyImpl implements _$ReportBundleCWProxy {
  const _$ReportBundleCWProxyImpl(this._value);

  final ReportBundle _value;

  @override
  ReportBundle report(Report report) => call(report: report);

  @override
  ReportBundle revision(int revision) => call(revision: revision);

  @override
  ReportBundle tlp(Tlp tlp) => call(tlp: tlp);

  @override
  ReportBundle envelopes(List<ObjectEnvelope> envelopes) =>
      call(envelopes: envelopes);

  @override
  ReportBundle snapshots(Map<String, Object>? snapshots) =>
      call(snapshots: snapshots);

  @override
  ReportBundle attachments(List<ReportBundleAttachmentsInner>? attachments) =>
      call(attachments: attachments);

  @override
  ReportBundle manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  ReportBundle missing(List<String>? missing) => call(missing: missing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBundle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBundle(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBundle call({
    Object? report = const $CopyWithPlaceholder(),
    Object? revision = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? envelopes = const $CopyWithPlaceholder(),
    Object? snapshots = const $CopyWithPlaceholder(),
    Object? attachments = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? missing = const $CopyWithPlaceholder(),
  }) {
    return ReportBundle(
      report: report == const $CopyWithPlaceholder() || report == null
          ? _value.report
          // ignore: cast_nullable_to_non_nullable
          : report as Report,
      revision: revision == const $CopyWithPlaceholder() || revision == null
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      envelopes: envelopes == const $CopyWithPlaceholder() || envelopes == null
          ? _value.envelopes
          // ignore: cast_nullable_to_non_nullable
          : envelopes as List<ObjectEnvelope>,
      snapshots: snapshots == const $CopyWithPlaceholder()
          ? _value.snapshots
          // ignore: cast_nullable_to_non_nullable
          : snapshots as Map<String, Object>?,
      attachments: attachments == const $CopyWithPlaceholder()
          ? _value.attachments
          // ignore: cast_nullable_to_non_nullable
          : attachments as List<ReportBundleAttachmentsInner>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      missing: missing == const $CopyWithPlaceholder()
          ? _value.missing
          // ignore: cast_nullable_to_non_nullable
          : missing as List<String>?,
    );
  }
}

extension $ReportBundleCopyWith on ReportBundle {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBundle.copyWith(...)` or `instanceOfReportBundle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBundleCWProxy get copyWith => _$ReportBundleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBundle _$ReportBundleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBundle', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['report', 'revision', 'tlp', 'envelopes'],
      );
      final val = ReportBundle(
        report: $checkedConvert(
          'report',
          (v) => Report.fromJson(v as Map<String, dynamic>),
        ),
        revision: $checkedConvert('revision', (v) => (v as num).toInt()),
        tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
        envelopes: $checkedConvert(
          'envelopes',
          (v) => (v as List<dynamic>)
              .map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        snapshots: $checkedConvert(
          'snapshots',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        attachments: $checkedConvert(
          'attachments',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => ReportBundleAttachmentsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        missing: $checkedConvert(
          'missing',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBundleToJson(ReportBundle instance) =>
    <String, dynamic>{
      'report': instance.report.toJson(),
      'revision': instance.revision,
      'tlp': _$TlpEnumMap[instance.tlp]!,
      'envelopes': instance.envelopes.map((e) => e.toJson()).toList(),
      'snapshots': ?instance.snapshots,
      'attachments': ?instance.attachments?.map((e) => e.toJson()).toList(),
      'manifestId': ?instance.manifestId,
      'missing': ?instance.missing,
    };

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
