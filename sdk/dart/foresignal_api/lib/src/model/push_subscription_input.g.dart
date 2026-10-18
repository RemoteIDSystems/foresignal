// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_subscription_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushSubscriptionInputCWProxy {
  PushSubscriptionInput name(String? name);

  PushSubscriptionInput subscription(
    PushSubscriptionInputSubscription subscription,
  );

  PushSubscriptionInput filters(PushFilters? filters);

  PushSubscriptionInput transportLabel(
    PushSubscriptionInputTransportLabelEnum? transportLabel,
  );

  PushSubscriptionInput payloadFormat(
    PushSubscriptionInputPayloadFormatEnum? payloadFormat,
  );

  PushSubscriptionInput lang(String? lang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PushSubscriptionInput call({
    String? name,
    PushSubscriptionInputSubscription subscription,
    PushFilters? filters,
    PushSubscriptionInputTransportLabelEnum? transportLabel,
    PushSubscriptionInputPayloadFormatEnum? payloadFormat,
    String? lang,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushSubscriptionInput.copyWith(...)` or call `instanceOfPushSubscriptionInput.copyWith.fieldName(value)` for a single field.
class _$PushSubscriptionInputCWProxyImpl
    implements _$PushSubscriptionInputCWProxy {
  const _$PushSubscriptionInputCWProxyImpl(this._value);

  final PushSubscriptionInput _value;

  @override
  PushSubscriptionInput name(String? name) => call(name: name);

  @override
  PushSubscriptionInput subscription(
    PushSubscriptionInputSubscription subscription,
  ) => call(subscription: subscription);

  @override
  PushSubscriptionInput filters(PushFilters? filters) => call(filters: filters);

  @override
  PushSubscriptionInput transportLabel(
    PushSubscriptionInputTransportLabelEnum? transportLabel,
  ) => call(transportLabel: transportLabel);

  @override
  PushSubscriptionInput payloadFormat(
    PushSubscriptionInputPayloadFormatEnum? payloadFormat,
  ) => call(payloadFormat: payloadFormat);

  @override
  PushSubscriptionInput lang(String? lang) => call(lang: lang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushSubscriptionInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? subscription = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? transportLabel = const $CopyWithPlaceholder(),
    Object? payloadFormat = const $CopyWithPlaceholder(),
    Object? lang = const $CopyWithPlaceholder(),
  }) {
    return PushSubscriptionInput(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      subscription:
          subscription == const $CopyWithPlaceholder() || subscription == null
          ? _value.subscription
          // ignore: cast_nullable_to_non_nullable
          : subscription as PushSubscriptionInputSubscription,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as PushFilters?,
      transportLabel: transportLabel == const $CopyWithPlaceholder()
          ? _value.transportLabel
          // ignore: cast_nullable_to_non_nullable
          : transportLabel as PushSubscriptionInputTransportLabelEnum?,
      payloadFormat: payloadFormat == const $CopyWithPlaceholder()
          ? _value.payloadFormat
          // ignore: cast_nullable_to_non_nullable
          : payloadFormat as PushSubscriptionInputPayloadFormatEnum?,
      lang: lang == const $CopyWithPlaceholder()
          ? _value.lang
          // ignore: cast_nullable_to_non_nullable
          : lang as String?,
    );
  }
}

extension $PushSubscriptionInputCopyWith on PushSubscriptionInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushSubscriptionInput.copyWith(...)` or `instanceOfPushSubscriptionInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushSubscriptionInputCWProxy get copyWith =>
      _$PushSubscriptionInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushSubscriptionInput _$PushSubscriptionInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushSubscriptionInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['subscription']);
  final val = PushSubscriptionInput(
    name: $checkedConvert('name', (v) => v as String?),
    subscription: $checkedConvert(
      'subscription',
      (v) =>
          PushSubscriptionInputSubscription.fromJson(v as Map<String, dynamic>),
    ),
    filters: $checkedConvert(
      'filters',
      (v) => v == null ? null : PushFilters.fromJson(v as Map<String, dynamic>),
    ),
    transportLabel: $checkedConvert(
      'transportLabel',
      (v) => $enumDecodeNullable(
        _$PushSubscriptionInputTransportLabelEnumEnumMap,
        v,
      ),
    ),
    payloadFormat: $checkedConvert(
      'payloadFormat',
      (v) => $enumDecodeNullable(
        _$PushSubscriptionInputPayloadFormatEnumEnumMap,
        v,
      ),
    ),
    lang: $checkedConvert('lang', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PushSubscriptionInputToJson(
  PushSubscriptionInput instance,
) => <String, dynamic>{
  'name': ?instance.name,
  'subscription': instance.subscription.toJson(),
  'filters': ?instance.filters?.toJson(),
  'transportLabel':
      ?_$PushSubscriptionInputTransportLabelEnumEnumMap[instance
          .transportLabel],
  'payloadFormat':
      ?_$PushSubscriptionInputPayloadFormatEnumEnumMap[instance.payloadFormat],
  'lang': ?instance.lang,
};

const _$PushSubscriptionInputTransportLabelEnumEnumMap = {
  PushSubscriptionInputTransportLabelEnum.browser: 'browser',
  PushSubscriptionInputTransportLabelEnum.unifiedpush: 'unifiedpush',
  PushSubscriptionInputTransportLabelEnum.other: 'other',
};

const _$PushSubscriptionInputPayloadFormatEnumEnumMap = {
  PushSubscriptionInputPayloadFormatEnum.foresignal: 'foresignal',
  PushSubscriptionInputPayloadFormatEnum.declarative: 'declarative',
};
