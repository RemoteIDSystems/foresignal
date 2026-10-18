// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'purge_report.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PurgeReportCWProxy {
  PurgeReport tombstoneSeq(int tombstoneSeq);

  PurgeReport versionsDeleted(int versionsDeleted);

  PurgeReport alertsRedacted(int? alertsRedacted);

  PurgeReport alertsDeleted(int? alertsDeleted);

  PurgeReport embeddingsDeleted(int? embeddingsDeleted);

  PurgeReport notificationsRedacted(int? notificationsRedacted);

  PurgeReport citingDocuments(PurgeReportCitingDocuments? citingDocuments);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PurgeReport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PurgeReport(...).copyWith(id: 12, name: "My name")
  /// ```
  PurgeReport call({
    int tombstoneSeq,
    int versionsDeleted,
    int? alertsRedacted,
    int? alertsDeleted,
    int? embeddingsDeleted,
    int? notificationsRedacted,
    PurgeReportCitingDocuments? citingDocuments,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPurgeReport.copyWith(...)` or call `instanceOfPurgeReport.copyWith.fieldName(value)` for a single field.
class _$PurgeReportCWProxyImpl implements _$PurgeReportCWProxy {
  const _$PurgeReportCWProxyImpl(this._value);

  final PurgeReport _value;

  @override
  PurgeReport tombstoneSeq(int tombstoneSeq) =>
      call(tombstoneSeq: tombstoneSeq);

  @override
  PurgeReport versionsDeleted(int versionsDeleted) =>
      call(versionsDeleted: versionsDeleted);

  @override
  PurgeReport alertsRedacted(int? alertsRedacted) =>
      call(alertsRedacted: alertsRedacted);

  @override
  PurgeReport alertsDeleted(int? alertsDeleted) =>
      call(alertsDeleted: alertsDeleted);

  @override
  PurgeReport embeddingsDeleted(int? embeddingsDeleted) =>
      call(embeddingsDeleted: embeddingsDeleted);

  @override
  PurgeReport notificationsRedacted(int? notificationsRedacted) =>
      call(notificationsRedacted: notificationsRedacted);

  @override
  PurgeReport citingDocuments(PurgeReportCitingDocuments? citingDocuments) =>
      call(citingDocuments: citingDocuments);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PurgeReport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PurgeReport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PurgeReport call({
    Object? tombstoneSeq = const $CopyWithPlaceholder(),
    Object? versionsDeleted = const $CopyWithPlaceholder(),
    Object? alertsRedacted = const $CopyWithPlaceholder(),
    Object? alertsDeleted = const $CopyWithPlaceholder(),
    Object? embeddingsDeleted = const $CopyWithPlaceholder(),
    Object? notificationsRedacted = const $CopyWithPlaceholder(),
    Object? citingDocuments = const $CopyWithPlaceholder(),
  }) {
    return PurgeReport(
      tombstoneSeq:
          tombstoneSeq == const $CopyWithPlaceholder() || tombstoneSeq == null
          ? _value.tombstoneSeq
          // ignore: cast_nullable_to_non_nullable
          : tombstoneSeq as int,
      versionsDeleted:
          versionsDeleted == const $CopyWithPlaceholder() ||
              versionsDeleted == null
          ? _value.versionsDeleted
          // ignore: cast_nullable_to_non_nullable
          : versionsDeleted as int,
      alertsRedacted: alertsRedacted == const $CopyWithPlaceholder()
          ? _value.alertsRedacted
          // ignore: cast_nullable_to_non_nullable
          : alertsRedacted as int?,
      alertsDeleted: alertsDeleted == const $CopyWithPlaceholder()
          ? _value.alertsDeleted
          // ignore: cast_nullable_to_non_nullable
          : alertsDeleted as int?,
      embeddingsDeleted: embeddingsDeleted == const $CopyWithPlaceholder()
          ? _value.embeddingsDeleted
          // ignore: cast_nullable_to_non_nullable
          : embeddingsDeleted as int?,
      notificationsRedacted:
          notificationsRedacted == const $CopyWithPlaceholder()
          ? _value.notificationsRedacted
          // ignore: cast_nullable_to_non_nullable
          : notificationsRedacted as int?,
      citingDocuments: citingDocuments == const $CopyWithPlaceholder()
          ? _value.citingDocuments
          // ignore: cast_nullable_to_non_nullable
          : citingDocuments as PurgeReportCitingDocuments?,
    );
  }
}

extension $PurgeReportCopyWith on PurgeReport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPurgeReport.copyWith(...)` or `instanceOfPurgeReport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PurgeReportCWProxy get copyWith => _$PurgeReportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PurgeReport _$PurgeReportFromJson(Map<String, dynamic> json) => $checkedCreate(
  'PurgeReport',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['tombstoneSeq', 'versionsDeleted']);
    final val = PurgeReport(
      tombstoneSeq: $checkedConvert('tombstoneSeq', (v) => (v as num).toInt()),
      versionsDeleted: $checkedConvert(
        'versionsDeleted',
        (v) => (v as num).toInt(),
      ),
      alertsRedacted: $checkedConvert(
        'alertsRedacted',
        (v) => (v as num?)?.toInt(),
      ),
      alertsDeleted: $checkedConvert(
        'alertsDeleted',
        (v) => (v as num?)?.toInt(),
      ),
      embeddingsDeleted: $checkedConvert(
        'embeddingsDeleted',
        (v) => (v as num?)?.toInt(),
      ),
      notificationsRedacted: $checkedConvert(
        'notificationsRedacted',
        (v) => (v as num?)?.toInt(),
      ),
      citingDocuments: $checkedConvert(
        'citingDocuments',
        (v) => v == null
            ? null
            : PurgeReportCitingDocuments.fromJson(v as Map<String, dynamic>),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$PurgeReportToJson(PurgeReport instance) =>
    <String, dynamic>{
      'tombstoneSeq': instance.tombstoneSeq,
      'versionsDeleted': instance.versionsDeleted,
      'alertsRedacted': ?instance.alertsRedacted,
      'alertsDeleted': ?instance.alertsDeleted,
      'embeddingsDeleted': ?instance.embeddingsDeleted,
      'notificationsRedacted': ?instance.notificationsRedacted,
      'citingDocuments': ?instance.citingDocuments?.toJson(),
    };
