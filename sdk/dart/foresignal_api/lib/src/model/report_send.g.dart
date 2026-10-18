// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_send.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportSendCWProxy {
  ReportSend id(String id);

  ReportSend reportId(String reportId);

  ReportSend revision(int? revision);

  ReportSend sentBy(PrincipalSummary sentBy);

  ReportSend sentAt(DateTime sentAt);

  ReportSend message(String? message);

  ReportSend recipients(List<ReportSendRecipientsInner> recipients);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSend(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportSend call({
    String id,
    String reportId,
    int? revision,
    PrincipalSummary sentBy,
    DateTime sentAt,
    String? message,
    List<ReportSendRecipientsInner> recipients,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportSend.copyWith(...)` or call `instanceOfReportSend.copyWith.fieldName(value)` for a single field.
class _$ReportSendCWProxyImpl implements _$ReportSendCWProxy {
  const _$ReportSendCWProxyImpl(this._value);

  final ReportSend _value;

  @override
  ReportSend id(String id) => call(id: id);

  @override
  ReportSend reportId(String reportId) => call(reportId: reportId);

  @override
  ReportSend revision(int? revision) => call(revision: revision);

  @override
  ReportSend sentBy(PrincipalSummary sentBy) => call(sentBy: sentBy);

  @override
  ReportSend sentAt(DateTime sentAt) => call(sentAt: sentAt);

  @override
  ReportSend message(String? message) => call(message: message);

  @override
  ReportSend recipients(List<ReportSendRecipientsInner> recipients) =>
      call(recipients: recipients);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSend(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportSend call({
    Object? id = const $CopyWithPlaceholder(),
    Object? reportId = const $CopyWithPlaceholder(),
    Object? revision = const $CopyWithPlaceholder(),
    Object? sentBy = const $CopyWithPlaceholder(),
    Object? sentAt = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? recipients = const $CopyWithPlaceholder(),
  }) {
    return ReportSend(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      reportId: reportId == const $CopyWithPlaceholder() || reportId == null
          ? _value.reportId
          // ignore: cast_nullable_to_non_nullable
          : reportId as String,
      revision: revision == const $CopyWithPlaceholder()
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int?,
      sentBy: sentBy == const $CopyWithPlaceholder() || sentBy == null
          ? _value.sentBy
          // ignore: cast_nullable_to_non_nullable
          : sentBy as PrincipalSummary,
      sentAt: sentAt == const $CopyWithPlaceholder() || sentAt == null
          ? _value.sentAt
          // ignore: cast_nullable_to_non_nullable
          : sentAt as DateTime,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      recipients:
          recipients == const $CopyWithPlaceholder() || recipients == null
          ? _value.recipients
          // ignore: cast_nullable_to_non_nullable
          : recipients as List<ReportSendRecipientsInner>,
    );
  }
}

extension $ReportSendCopyWith on ReportSend {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportSend.copyWith(...)` or `instanceOfReportSend.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportSendCWProxy get copyWith => _$ReportSendCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportSend _$ReportSendFromJson(Map<String, dynamic> json) => $checkedCreate(
  'ReportSend',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const ['id', 'reportId', 'sentBy', 'sentAt', 'recipients'],
    );
    final val = ReportSend(
      id: $checkedConvert('id', (v) => v as String),
      reportId: $checkedConvert('reportId', (v) => v as String),
      revision: $checkedConvert('revision', (v) => (v as num?)?.toInt()),
      sentBy: $checkedConvert(
        'sentBy',
        (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
      ),
      sentAt: $checkedConvert('sentAt', (v) => DateTime.parse(v as String)),
      message: $checkedConvert('message', (v) => v as String?),
      recipients: $checkedConvert(
        'recipients',
        (v) => (v as List<dynamic>)
            .map(
              (e) =>
                  ReportSendRecipientsInner.fromJson(e as Map<String, dynamic>),
            )
            .toList(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$ReportSendToJson(ReportSend instance) =>
    <String, dynamic>{
      'id': instance.id,
      'reportId': instance.reportId,
      'revision': ?instance.revision,
      'sentBy': instance.sentBy.toJson(),
      'sentAt': instance.sentAt.toIso8601String(),
      'message': ?instance.message,
      'recipients': instance.recipients.map((e) => e.toJson()).toList(),
    };
