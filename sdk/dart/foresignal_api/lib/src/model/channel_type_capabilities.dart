//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_type_capabilities.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelTypeCapabilities {
  /// Returns a new [ChannelTypeCapabilities] instance.
  ChannelTypeCapabilities({

     this.editInPlace,

     this.attachments,

     this.fanOut,
  });

  @JsonKey(
    
    name: r'editInPlace',
    required: false,
    includeIfNull: false,
  )


  final bool? editInPlace;



  @JsonKey(
    
    name: r'attachments',
    required: false,
    includeIfNull: false,
  )


  final bool? attachments;



  @JsonKey(
    
    name: r'fanOut',
    required: false,
    includeIfNull: false,
  )


  final bool? fanOut;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelTypeCapabilities &&
      other.editInPlace == editInPlace &&
      other.attachments == attachments &&
      other.fanOut == fanOut;

    @override
    int get hashCode =>
        editInPlace.hashCode +
        attachments.hashCode +
        fanOut.hashCode;

  factory ChannelTypeCapabilities.fromJson(Map<String, dynamic> json) => _$ChannelTypeCapabilitiesFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelTypeCapabilitiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

