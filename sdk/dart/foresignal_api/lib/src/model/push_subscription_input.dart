//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_subscription_input_subscription.dart';
import 'package:foresignal_api/src/model/push_filters.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_subscription_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushSubscriptionInput {
  /// Returns a new [PushSubscriptionInput] instance.
  PushSubscriptionInput({

     this.name,

    required  this.subscription,

     this.filters,

     this.transportLabel,

     this.payloadFormat,

     this.lang,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'subscription',
    required: true,
    includeIfNull: false,
  )


  final PushSubscriptionInputSubscription subscription;



  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final PushFilters? filters;



      /// Display and filter only; the wire protocol is identical
  @JsonKey(
    
    name: r'transportLabel',
    required: false,
    includeIfNull: false,
  )


  final PushSubscriptionInputTransportLabelEnum? transportLabel;



  @JsonKey(
    
    name: r'payloadFormat',
    required: false,
    includeIfNull: false,
  )


  final PushSubscriptionInputPayloadFormatEnum? payloadFormat;



  @JsonKey(
    
    name: r'lang',
    required: false,
    includeIfNull: false,
  )


  final String? lang;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushSubscriptionInput &&
      other.name == name &&
      other.subscription == subscription &&
      other.filters == filters &&
      other.transportLabel == transportLabel &&
      other.payloadFormat == payloadFormat &&
      other.lang == lang;

    @override
    int get hashCode =>
        name.hashCode +
        subscription.hashCode +
        filters.hashCode +
        transportLabel.hashCode +
        payloadFormat.hashCode +
        (lang == null ? 0 : lang.hashCode);

  factory PushSubscriptionInput.fromJson(Map<String, dynamic> json) => _$PushSubscriptionInputFromJson(json);

  Map<String, dynamic> toJson() => _$PushSubscriptionInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Display and filter only; the wire protocol is identical
enum PushSubscriptionInputTransportLabelEnum {
@JsonValue(r'browser')
browser(r'browser'),
@JsonValue(r'unifiedpush')
unifiedpush(r'unifiedpush'),
@JsonValue(r'other')
other(r'other');

const PushSubscriptionInputTransportLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushSubscriptionInputPayloadFormatEnum {
@JsonValue(r'foresignal')
foresignal(r'foresignal'),
@JsonValue(r'declarative')
declarative(r'declarative');

const PushSubscriptionInputPayloadFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


