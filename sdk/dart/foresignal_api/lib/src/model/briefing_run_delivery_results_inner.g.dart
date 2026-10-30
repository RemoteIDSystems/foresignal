// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_run_delivery_results_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingRunDeliveryResultsInnerCWProxy {
  BriefingRunDeliveryResultsInner kind(
    BriefingRunDeliveryResultsInnerKindEnum kind,
  );

  BriefingRunDeliveryResultsInner channelId(String? channelId);

  BriefingRunDeliveryResultsInner format(
    BriefingRunDeliveryResultsInnerFormatEnum format,
  );

  BriefingRunDeliveryResultsInner status(
    BriefingRunDeliveryResultsInnerStatusEnum status,
  );

  BriefingRunDeliveryResultsInner notificationId(int? notificationId);

  BriefingRunDeliveryResultsInner url(String? url);

  BriefingRunDeliveryResultsInner error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunDeliveryResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunDeliveryResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingRunDeliveryResultsInner call({
    BriefingRunDeliveryResultsInnerKindEnum kind,
    String? channelId,
    BriefingRunDeliveryResultsInnerFormatEnum format,
    BriefingRunDeliveryResultsInnerStatusEnum status,
    int? notificationId,
    String? url,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingRunDeliveryResultsInner.copyWith(...)` or call `instanceOfBriefingRunDeliveryResultsInner.copyWith.fieldName(value)` for a single field.
class _$BriefingRunDeliveryResultsInnerCWProxyImpl
    implements _$BriefingRunDeliveryResultsInnerCWProxy {
  const _$BriefingRunDeliveryResultsInnerCWProxyImpl(this._value);

  final BriefingRunDeliveryResultsInner _value;

  @override
  BriefingRunDeliveryResultsInner kind(
    BriefingRunDeliveryResultsInnerKindEnum kind,
  ) => call(kind: kind);

  @override
  BriefingRunDeliveryResultsInner channelId(String? channelId) =>
      call(channelId: channelId);

  @override
  BriefingRunDeliveryResultsInner format(
    BriefingRunDeliveryResultsInnerFormatEnum format,
  ) => call(format: format);

  @override
  BriefingRunDeliveryResultsInner status(
    BriefingRunDeliveryResultsInnerStatusEnum status,
  ) => call(status: status);

  @override
  BriefingRunDeliveryResultsInner notificationId(int? notificationId) =>
      call(notificationId: notificationId);

  @override
  BriefingRunDeliveryResultsInner url(String? url) => call(url: url);

  @override
  BriefingRunDeliveryResultsInner error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunDeliveryResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunDeliveryResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingRunDeliveryResultsInner call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? channelId = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? notificationId = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return BriefingRunDeliveryResultsInner(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as BriefingRunDeliveryResultsInnerKindEnum,
      channelId: channelId == const $CopyWithPlaceholder()
          ? _value.channelId
          // ignore: cast_nullable_to_non_nullable
          : channelId as String?,
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as BriefingRunDeliveryResultsInnerFormatEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BriefingRunDeliveryResultsInnerStatusEnum,
      notificationId: notificationId == const $CopyWithPlaceholder()
          ? _value.notificationId
          // ignore: cast_nullable_to_non_nullable
          : notificationId as int?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $BriefingRunDeliveryResultsInnerCopyWith
    on BriefingRunDeliveryResultsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingRunDeliveryResultsInner.copyWith(...)` or `instanceOfBriefingRunDeliveryResultsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingRunDeliveryResultsInnerCWProxy get copyWith =>
      _$BriefingRunDeliveryResultsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingRunDeliveryResultsInner _$BriefingRunDeliveryResultsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BriefingRunDeliveryResultsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['kind', 'format', 'status']);
  final val = BriefingRunDeliveryResultsInner(
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$BriefingRunDeliveryResultsInnerKindEnumEnumMap, v),
    ),
    channelId: $checkedConvert('channelId', (v) => v as String?),
    format: $checkedConvert(
      'format',
      (v) => $enumDecode(_$BriefingRunDeliveryResultsInnerFormatEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$BriefingRunDeliveryResultsInnerStatusEnumEnumMap, v),
    ),
    notificationId: $checkedConvert(
      'notificationId',
      (v) => (v as num?)?.toInt(),
    ),
    url: $checkedConvert('url', (v) => v as String?),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$BriefingRunDeliveryResultsInnerToJson(
  BriefingRunDeliveryResultsInner instance,
) => <String, dynamic>{
  'kind': _$BriefingRunDeliveryResultsInnerKindEnumEnumMap[instance.kind]!,
  'channelId': ?instance.channelId,
  'format':
      _$BriefingRunDeliveryResultsInnerFormatEnumEnumMap[instance.format]!,
  'status':
      _$BriefingRunDeliveryResultsInnerStatusEnumEnumMap[instance.status]!,
  'notificationId': ?instance.notificationId,
  'url': ?instance.url,
  'error': ?instance.error,
};

const _$BriefingRunDeliveryResultsInnerKindEnumEnumMap = {
  BriefingRunDeliveryResultsInnerKindEnum.channel: 'channel',
  BriefingRunDeliveryResultsInnerKindEnum.website: 'website',
  BriefingRunDeliveryResultsInnerKindEnum.podcast: 'podcast',
};

const _$BriefingRunDeliveryResultsInnerFormatEnumEnumMap = {
  BriefingRunDeliveryResultsInnerFormatEnum.email: 'email',
  BriefingRunDeliveryResultsInnerFormatEnum.push: 'push',
  BriefingRunDeliveryResultsInnerFormatEnum.alert: 'alert',
  BriefingRunDeliveryResultsInnerFormatEnum.html: 'html',
  BriefingRunDeliveryResultsInnerFormatEnum.audio: 'audio',
};

const _$BriefingRunDeliveryResultsInnerStatusEnumEnumMap = {
  BriefingRunDeliveryResultsInnerStatusEnum.queued: 'queued',
  BriefingRunDeliveryResultsInnerStatusEnum.available: 'available',
  BriefingRunDeliveryResultsInnerStatusEnum.skipped: 'skipped',
  BriefingRunDeliveryResultsInnerStatusEnum.failed: 'failed',
};
