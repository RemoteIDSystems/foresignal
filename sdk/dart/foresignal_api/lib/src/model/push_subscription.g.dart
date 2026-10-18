// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_subscription.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushSubscriptionCWProxy {
  PushSubscription id(String id);

  PushSubscription name(String? name);

  PushSubscription endpointHost(String endpointHost);

  PushSubscription filters(PushFilters? filters);

  PushSubscription createdAt(DateTime createdAt);

  PushSubscription lastSuccessAt(DateTime? lastSuccessAt);

  PushSubscription status(PushSubscriptionStatusEnum? status);

  PushSubscription transportLabel(
    PushSubscriptionTransportLabelEnum? transportLabel,
  );

  PushSubscription payloadFormat(
    PushSubscriptionPayloadFormatEnum? payloadFormat,
  );

  PushSubscription lang(String? lang);

  PushSubscription failureCount(int? failureCount);

  PushSubscription lastFailureAt(DateTime? lastFailureAt);

  PushSubscription lastStatus(int? lastStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscription(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscription(...).copyWith(id: 12, name: "My name")
  /// ```
  PushSubscription call({
    String id,
    String? name,
    String endpointHost,
    PushFilters? filters,
    DateTime createdAt,
    DateTime? lastSuccessAt,
    PushSubscriptionStatusEnum? status,
    PushSubscriptionTransportLabelEnum? transportLabel,
    PushSubscriptionPayloadFormatEnum? payloadFormat,
    String? lang,
    int? failureCount,
    DateTime? lastFailureAt,
    int? lastStatus,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushSubscription.copyWith(...)` or call `instanceOfPushSubscription.copyWith.fieldName(value)` for a single field.
class _$PushSubscriptionCWProxyImpl implements _$PushSubscriptionCWProxy {
  const _$PushSubscriptionCWProxyImpl(this._value);

  final PushSubscription _value;

  @override
  PushSubscription id(String id) => call(id: id);

  @override
  PushSubscription name(String? name) => call(name: name);

  @override
  PushSubscription endpointHost(String endpointHost) =>
      call(endpointHost: endpointHost);

  @override
  PushSubscription filters(PushFilters? filters) => call(filters: filters);

  @override
  PushSubscription createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  PushSubscription lastSuccessAt(DateTime? lastSuccessAt) =>
      call(lastSuccessAt: lastSuccessAt);

  @override
  PushSubscription status(PushSubscriptionStatusEnum? status) =>
      call(status: status);

  @override
  PushSubscription transportLabel(
    PushSubscriptionTransportLabelEnum? transportLabel,
  ) => call(transportLabel: transportLabel);

  @override
  PushSubscription payloadFormat(
    PushSubscriptionPayloadFormatEnum? payloadFormat,
  ) => call(payloadFormat: payloadFormat);

  @override
  PushSubscription lang(String? lang) => call(lang: lang);

  @override
  PushSubscription failureCount(int? failureCount) =>
      call(failureCount: failureCount);

  @override
  PushSubscription lastFailureAt(DateTime? lastFailureAt) =>
      call(lastFailureAt: lastFailureAt);

  @override
  PushSubscription lastStatus(int? lastStatus) => call(lastStatus: lastStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscription(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscription(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushSubscription call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? endpointHost = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastSuccessAt = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? transportLabel = const $CopyWithPlaceholder(),
    Object? payloadFormat = const $CopyWithPlaceholder(),
    Object? lang = const $CopyWithPlaceholder(),
    Object? failureCount = const $CopyWithPlaceholder(),
    Object? lastFailureAt = const $CopyWithPlaceholder(),
    Object? lastStatus = const $CopyWithPlaceholder(),
  }) {
    return PushSubscription(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      endpointHost:
          endpointHost == const $CopyWithPlaceholder() || endpointHost == null
          ? _value.endpointHost
          // ignore: cast_nullable_to_non_nullable
          : endpointHost as String,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as PushFilters?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastSuccessAt: lastSuccessAt == const $CopyWithPlaceholder()
          ? _value.lastSuccessAt
          // ignore: cast_nullable_to_non_nullable
          : lastSuccessAt as DateTime?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as PushSubscriptionStatusEnum?,
      transportLabel: transportLabel == const $CopyWithPlaceholder()
          ? _value.transportLabel
          // ignore: cast_nullable_to_non_nullable
          : transportLabel as PushSubscriptionTransportLabelEnum?,
      payloadFormat: payloadFormat == const $CopyWithPlaceholder()
          ? _value.payloadFormat
          // ignore: cast_nullable_to_non_nullable
          : payloadFormat as PushSubscriptionPayloadFormatEnum?,
      lang: lang == const $CopyWithPlaceholder()
          ? _value.lang
          // ignore: cast_nullable_to_non_nullable
          : lang as String?,
      failureCount: failureCount == const $CopyWithPlaceholder()
          ? _value.failureCount
          // ignore: cast_nullable_to_non_nullable
          : failureCount as int?,
      lastFailureAt: lastFailureAt == const $CopyWithPlaceholder()
          ? _value.lastFailureAt
          // ignore: cast_nullable_to_non_nullable
          : lastFailureAt as DateTime?,
      lastStatus: lastStatus == const $CopyWithPlaceholder()
          ? _value.lastStatus
          // ignore: cast_nullable_to_non_nullable
          : lastStatus as int?,
    );
  }
}

extension $PushSubscriptionCopyWith on PushSubscription {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushSubscription.copyWith(...)` or `instanceOfPushSubscription.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushSubscriptionCWProxy get copyWith => _$PushSubscriptionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushSubscription _$PushSubscriptionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushSubscription', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'endpointHost', 'createdAt']);
  final val = PushSubscription(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    endpointHost: $checkedConvert('endpointHost', (v) => v as String),
    filters: $checkedConvert(
      'filters',
      (v) => v == null ? null : PushFilters.fromJson(v as Map<String, dynamic>),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    lastSuccessAt: $checkedConvert(
      'lastSuccessAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecodeNullable(_$PushSubscriptionStatusEnumEnumMap, v),
    ),
    transportLabel: $checkedConvert(
      'transportLabel',
      (v) =>
          $enumDecodeNullable(_$PushSubscriptionTransportLabelEnumEnumMap, v),
    ),
    payloadFormat: $checkedConvert(
      'payloadFormat',
      (v) => $enumDecodeNullable(_$PushSubscriptionPayloadFormatEnumEnumMap, v),
    ),
    lang: $checkedConvert('lang', (v) => v as String?),
    failureCount: $checkedConvert('failureCount', (v) => (v as num?)?.toInt()),
    lastFailureAt: $checkedConvert(
      'lastFailureAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastStatus: $checkedConvert('lastStatus', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PushSubscriptionToJson(PushSubscription instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': ?instance.name,
      'endpointHost': instance.endpointHost,
      'filters': ?instance.filters?.toJson(),
      'createdAt': instance.createdAt.toIso8601String(),
      'lastSuccessAt': ?instance.lastSuccessAt?.toIso8601String(),
      'status': ?_$PushSubscriptionStatusEnumEnumMap[instance.status],
      'transportLabel':
          ?_$PushSubscriptionTransportLabelEnumEnumMap[instance.transportLabel],
      'payloadFormat':
          ?_$PushSubscriptionPayloadFormatEnumEnumMap[instance.payloadFormat],
      'lang': ?instance.lang,
      'failureCount': ?instance.failureCount,
      'lastFailureAt': ?instance.lastFailureAt?.toIso8601String(),
      'lastStatus': ?instance.lastStatus,
    };

const _$PushSubscriptionStatusEnumEnumMap = {
  PushSubscriptionStatusEnum.pending: 'pending',
  PushSubscriptionStatusEnum.active: 'active',
  PushSubscriptionStatusEnum.disabled: 'disabled',
};

const _$PushSubscriptionTransportLabelEnumEnumMap = {
  PushSubscriptionTransportLabelEnum.browser: 'browser',
  PushSubscriptionTransportLabelEnum.unifiedpush: 'unifiedpush',
  PushSubscriptionTransportLabelEnum.other: 'other',
};

const _$PushSubscriptionPayloadFormatEnumEnumMap = {
  PushSubscriptionPayloadFormatEnum.foresignal: 'foresignal',
  PushSubscriptionPayloadFormatEnum.declarative: 'declarative',
};
