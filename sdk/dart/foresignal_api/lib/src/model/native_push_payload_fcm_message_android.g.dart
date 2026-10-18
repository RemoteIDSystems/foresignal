// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_fcm_message_android.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadFcmMessageAndroidCWProxy {
  NativePushPayloadFcmMessageAndroid priority(
    NativePushPayloadFcmMessageAndroidPriorityEnum? priority,
  );

  NativePushPayloadFcmMessageAndroid ttl(String? ttl);

  NativePushPayloadFcmMessageAndroid collapseKey(String? collapseKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcmMessageAndroid(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcmMessageAndroid(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadFcmMessageAndroid call({
    NativePushPayloadFcmMessageAndroidPriorityEnum? priority,
    String? ttl,
    String? collapseKey,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadFcmMessageAndroid.copyWith(...)` or call `instanceOfNativePushPayloadFcmMessageAndroid.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadFcmMessageAndroidCWProxyImpl
    implements _$NativePushPayloadFcmMessageAndroidCWProxy {
  const _$NativePushPayloadFcmMessageAndroidCWProxyImpl(this._value);

  final NativePushPayloadFcmMessageAndroid _value;

  @override
  NativePushPayloadFcmMessageAndroid priority(
    NativePushPayloadFcmMessageAndroidPriorityEnum? priority,
  ) => call(priority: priority);

  @override
  NativePushPayloadFcmMessageAndroid ttl(String? ttl) => call(ttl: ttl);

  @override
  NativePushPayloadFcmMessageAndroid collapseKey(String? collapseKey) =>
      call(collapseKey: collapseKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcmMessageAndroid(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcmMessageAndroid(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadFcmMessageAndroid call({
    Object? priority = const $CopyWithPlaceholder(),
    Object? ttl = const $CopyWithPlaceholder(),
    Object? collapseKey = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayloadFcmMessageAndroid(
      priority: priority == const $CopyWithPlaceholder()
          ? _value.priority
          // ignore: cast_nullable_to_non_nullable
          : priority as NativePushPayloadFcmMessageAndroidPriorityEnum?,
      ttl: ttl == const $CopyWithPlaceholder()
          ? _value.ttl
          // ignore: cast_nullable_to_non_nullable
          : ttl as String?,
      collapseKey: collapseKey == const $CopyWithPlaceholder()
          ? _value.collapseKey
          // ignore: cast_nullable_to_non_nullable
          : collapseKey as String?,
    );
  }
}

extension $NativePushPayloadFcmMessageAndroidCopyWith
    on NativePushPayloadFcmMessageAndroid {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadFcmMessageAndroid.copyWith(...)` or `instanceOfNativePushPayloadFcmMessageAndroid.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadFcmMessageAndroidCWProxy get copyWith =>
      _$NativePushPayloadFcmMessageAndroidCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadFcmMessageAndroid _$NativePushPayloadFcmMessageAndroidFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NativePushPayloadFcmMessageAndroid', json, (
  $checkedConvert,
) {
  final val = NativePushPayloadFcmMessageAndroid(
    priority: $checkedConvert(
      'priority',
      (v) => $enumDecodeNullable(
        _$NativePushPayloadFcmMessageAndroidPriorityEnumEnumMap,
        v,
      ),
    ),
    ttl: $checkedConvert('ttl', (v) => v as String?),
    collapseKey: $checkedConvert('collapse_key', (v) => v as String?),
  );
  return val;
}, fieldKeyMap: const {'collapseKey': 'collapse_key'});

Map<String, dynamic> _$NativePushPayloadFcmMessageAndroidToJson(
  NativePushPayloadFcmMessageAndroid instance,
) => <String, dynamic>{
  'priority':
      ?_$NativePushPayloadFcmMessageAndroidPriorityEnumEnumMap[instance
          .priority],
  'ttl': ?instance.ttl,
  'collapse_key': ?instance.collapseKey,
};

const _$NativePushPayloadFcmMessageAndroidPriorityEnumEnumMap = {
  NativePushPayloadFcmMessageAndroidPriorityEnum.HIGH: 'HIGH',
  NativePushPayloadFcmMessageAndroidPriorityEnum.NORMAL: 'NORMAL',
};
