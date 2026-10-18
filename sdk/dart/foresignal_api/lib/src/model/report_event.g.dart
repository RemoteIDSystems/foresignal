// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportEventCWProxy {
  ReportEvent reportRef(String reportRef);

  ReportEvent title(String title);

  ReportEvent tlp(Tlp tlp);

  ReportEvent revision(int? revision);

  ReportEvent from(PrincipalSummary? from);

  ReportEvent message(String? message);

  ReportEvent reason(String? reason);

  ReportEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportEvent call({
    String reportRef,
    String title,
    Tlp tlp,
    int? revision,
    PrincipalSummary? from,
    String? message,
    String? reason,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportEvent.copyWith(...)` or call `instanceOfReportEvent.copyWith.fieldName(value)` for a single field.
class _$ReportEventCWProxyImpl implements _$ReportEventCWProxy {
  const _$ReportEventCWProxyImpl(this._value);

  final ReportEvent _value;

  @override
  ReportEvent reportRef(String reportRef) => call(reportRef: reportRef);

  @override
  ReportEvent title(String title) => call(title: title);

  @override
  ReportEvent tlp(Tlp tlp) => call(tlp: tlp);

  @override
  ReportEvent revision(int? revision) => call(revision: revision);

  @override
  ReportEvent from(PrincipalSummary? from) => call(from: from);

  @override
  ReportEvent message(String? message) => call(message: message);

  @override
  ReportEvent reason(String? reason) => call(reason: reason);

  @override
  ReportEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportEvent call({
    Object? reportRef = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? revision = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ReportEvent(
      reportRef: reportRef == const $CopyWithPlaceholder() || reportRef == null
          ? _value.reportRef
          // ignore: cast_nullable_to_non_nullable
          : reportRef as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      revision: revision == const $CopyWithPlaceholder()
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as PrincipalSummary?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ReportEventCopyWith on ReportEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportEvent.copyWith(...)` or `instanceOfReportEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportEventCWProxy get copyWith => _$ReportEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportEvent _$ReportEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['reportRef', 'title', 'tlp', 'at']);
      final val = ReportEvent(
        reportRef: $checkedConvert('reportRef', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String),
        tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
        revision: $checkedConvert('revision', (v) => (v as num?)?.toInt()),
        from: $checkedConvert(
          'from',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        message: $checkedConvert('message', (v) => v as String?),
        reason: $checkedConvert('reason', (v) => v as String?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$ReportEventToJson(ReportEvent instance) =>
    <String, dynamic>{
      'reportRef': instance.reportRef,
      'title': instance.title,
      'tlp': _$TlpEnumMap[instance.tlp]!,
      'revision': ?instance.revision,
      'from': ?instance.from?.toJson(),
      'message': ?instance.message,
      'reason': ?instance.reason,
      'at': instance.at.toIso8601String(),
    };

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
