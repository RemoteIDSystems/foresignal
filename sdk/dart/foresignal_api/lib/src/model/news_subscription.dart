//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'news_subscription.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NewsSubscription {
  /// Returns a new [NewsSubscription] instance.
  NewsSubscription({

    required  this.type,

    required  this.id,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final NewsSubscriptionTypeEnum type;



      /// A media outlet id (`listMediaOutlets`) for `outlet`; a BCP 47 language tag for `language`
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NewsSubscription &&
      other.type == type &&
      other.id == id;

    @override
    int get hashCode =>
        type.hashCode +
        id.hashCode;

  factory NewsSubscription.fromJson(Map<String, dynamic> json) => _$NewsSubscriptionFromJson(json);

  Map<String, dynamic> toJson() => _$NewsSubscriptionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NewsSubscriptionTypeEnum {
@JsonValue(r'outlet')
outlet(r'outlet'),
@JsonValue(r'language')
language(r'language');

const NewsSubscriptionTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


