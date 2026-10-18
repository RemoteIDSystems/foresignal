// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'purge_report_citing_documents.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PurgeReportCitingDocumentsCWProxy {
  PurgeReportCitingDocuments askMessages(int? askMessages);

  PurgeReportCitingDocuments briefingRuns(int? briefingRuns);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PurgeReportCitingDocuments(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PurgeReportCitingDocuments(...).copyWith(id: 12, name: "My name")
  /// ```
  PurgeReportCitingDocuments call({int? askMessages, int? briefingRuns});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPurgeReportCitingDocuments.copyWith(...)` or call `instanceOfPurgeReportCitingDocuments.copyWith.fieldName(value)` for a single field.
class _$PurgeReportCitingDocumentsCWProxyImpl
    implements _$PurgeReportCitingDocumentsCWProxy {
  const _$PurgeReportCitingDocumentsCWProxyImpl(this._value);

  final PurgeReportCitingDocuments _value;

  @override
  PurgeReportCitingDocuments askMessages(int? askMessages) =>
      call(askMessages: askMessages);

  @override
  PurgeReportCitingDocuments briefingRuns(int? briefingRuns) =>
      call(briefingRuns: briefingRuns);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PurgeReportCitingDocuments(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PurgeReportCitingDocuments(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PurgeReportCitingDocuments call({
    Object? askMessages = const $CopyWithPlaceholder(),
    Object? briefingRuns = const $CopyWithPlaceholder(),
  }) {
    return PurgeReportCitingDocuments(
      askMessages: askMessages == const $CopyWithPlaceholder()
          ? _value.askMessages
          // ignore: cast_nullable_to_non_nullable
          : askMessages as int?,
      briefingRuns: briefingRuns == const $CopyWithPlaceholder()
          ? _value.briefingRuns
          // ignore: cast_nullable_to_non_nullable
          : briefingRuns as int?,
    );
  }
}

extension $PurgeReportCitingDocumentsCopyWith on PurgeReportCitingDocuments {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPurgeReportCitingDocuments.copyWith(...)` or `instanceOfPurgeReportCitingDocuments.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PurgeReportCitingDocumentsCWProxy get copyWith =>
      _$PurgeReportCitingDocumentsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PurgeReportCitingDocuments _$PurgeReportCitingDocumentsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PurgeReportCitingDocuments', json, ($checkedConvert) {
  final val = PurgeReportCitingDocuments(
    askMessages: $checkedConvert('askMessages', (v) => (v as num?)?.toInt()),
    briefingRuns: $checkedConvert('briefingRuns', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PurgeReportCitingDocumentsToJson(
  PurgeReportCitingDocuments instance,
) => <String, dynamic>{
  'askMessages': ?instance.askMessages,
  'briefingRuns': ?instance.briefingRuns,
};
