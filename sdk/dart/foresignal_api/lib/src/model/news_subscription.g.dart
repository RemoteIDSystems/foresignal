// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news_subscription.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NewsSubscriptionCWProxy {
  NewsSubscription type(NewsSubscriptionTypeEnum type);

  NewsSubscription id(String id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsSubscription(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsSubscription(...).copyWith(id: 12, name: "My name")
  /// ```
  NewsSubscription call({NewsSubscriptionTypeEnum type, String id});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNewsSubscription.copyWith(...)` or call `instanceOfNewsSubscription.copyWith.fieldName(value)` for a single field.
class _$NewsSubscriptionCWProxyImpl implements _$NewsSubscriptionCWProxy {
  const _$NewsSubscriptionCWProxyImpl(this._value);

  final NewsSubscription _value;

  @override
  NewsSubscription type(NewsSubscriptionTypeEnum type) => call(type: type);

  @override
  NewsSubscription id(String id) => call(id: id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsSubscription(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsSubscription(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NewsSubscription call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
  }) {
    return NewsSubscription(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as NewsSubscriptionTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
    );
  }
}

extension $NewsSubscriptionCopyWith on NewsSubscription {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNewsSubscription.copyWith(...)` or `instanceOfNewsSubscription.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NewsSubscriptionCWProxy get copyWith => _$NewsSubscriptionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NewsSubscription _$NewsSubscriptionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NewsSubscription', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'id']);
      final val = NewsSubscription(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$NewsSubscriptionTypeEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$NewsSubscriptionToJson(NewsSubscription instance) =>
    <String, dynamic>{
      'type': _$NewsSubscriptionTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
    };

const _$NewsSubscriptionTypeEnumEnumMap = {
  NewsSubscriptionTypeEnum.outlet: 'outlet',
  NewsSubscriptionTypeEnum.language: 'language',
};
