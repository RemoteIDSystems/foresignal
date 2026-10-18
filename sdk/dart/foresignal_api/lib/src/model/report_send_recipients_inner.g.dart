// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_send_recipients_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportSendRecipientsInnerCWProxy {
  ReportSendRecipientsInner grantee(String grantee);

  ReportSendRecipientsInner status(ReportSendRecipientsInnerStatusEnum status);

  ReportSendRecipientsInner channelIds(List<String>? channelIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSendRecipientsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSendRecipientsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportSendRecipientsInner call({
    String grantee,
    ReportSendRecipientsInnerStatusEnum status,
    List<String>? channelIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportSendRecipientsInner.copyWith(...)` or call `instanceOfReportSendRecipientsInner.copyWith.fieldName(value)` for a single field.
class _$ReportSendRecipientsInnerCWProxyImpl
    implements _$ReportSendRecipientsInnerCWProxy {
  const _$ReportSendRecipientsInnerCWProxyImpl(this._value);

  final ReportSendRecipientsInner _value;

  @override
  ReportSendRecipientsInner grantee(String grantee) => call(grantee: grantee);

  @override
  ReportSendRecipientsInner status(
    ReportSendRecipientsInnerStatusEnum status,
  ) => call(status: status);

  @override
  ReportSendRecipientsInner channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSendRecipientsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSendRecipientsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportSendRecipientsInner call({
    Object? grantee = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? channelIds = const $CopyWithPlaceholder(),
  }) {
    return ReportSendRecipientsInner(
      grantee: grantee == const $CopyWithPlaceholder() || grantee == null
          ? _value.grantee
          // ignore: cast_nullable_to_non_nullable
          : grantee as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ReportSendRecipientsInnerStatusEnum,
      channelIds: channelIds == const $CopyWithPlaceholder()
          ? _value.channelIds
          // ignore: cast_nullable_to_non_nullable
          : channelIds as List<String>?,
    );
  }
}

extension $ReportSendRecipientsInnerCopyWith on ReportSendRecipientsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportSendRecipientsInner.copyWith(...)` or `instanceOfReportSendRecipientsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportSendRecipientsInnerCWProxy get copyWith =>
      _$ReportSendRecipientsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportSendRecipientsInner _$ReportSendRecipientsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportSendRecipientsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['grantee', 'status']);
  final val = ReportSendRecipientsInner(
    grantee: $checkedConvert('grantee', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ReportSendRecipientsInnerStatusEnumEnumMap, v),
    ),
    channelIds: $checkedConvert(
      'channelIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReportSendRecipientsInnerToJson(
  ReportSendRecipientsInner instance,
) => <String, dynamic>{
  'grantee': instance.grantee,
  'status': _$ReportSendRecipientsInnerStatusEnumEnumMap[instance.status]!,
  'channelIds': ?instance.channelIds,
};

const _$ReportSendRecipientsInnerStatusEnumEnumMap = {
  ReportSendRecipientsInnerStatusEnum.notified: 'notified',
  ReportSendRecipientsInnerStatusEnum.granted: 'granted',
  ReportSendRecipientsInnerStatusEnum.delivered: 'delivered',
  ReportSendRecipientsInnerStatusEnum.failed: 'failed',
};
